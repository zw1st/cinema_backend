package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.order.CreateOrderRq;
import com.example.demo.api.order.OrderRs;
import com.example.demo.api.order.SeatCoordRq;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SessionEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enumeration.OrderStatus;
import com.example.demo.entity.enumeration.TicketStatus;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.SessionRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, TicketRepository ticketRepository,
            SessionRepository sessionRepository, SeatRepository seatRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.sessionRepository = sessionRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderRs createOrder(CreateOrderRq rq, Long userId) {
        // 1. Пользователь
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(UserEntity.class, userId));

        // 2. Сеанс
        SessionEntity session = sessionRepository.findById(rq.sessionId())
                .orElseThrow(() -> new NotFoundException(SessionEntity.class, rq.sessionId()));

        LocalDateTime sessionStart = LocalDateTime.of(session.getDate(), session.getStartTime());
        if (sessionStart.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot book tickets for an already started session");
        }

        // 3. Валидация мест + расчёт суммы
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TicketEntity> tickets = new ArrayList<>();

        for (SeatCoordRq coord : rq.seats()) {
            // 1. Блокируем, если место активно забронировано или оплачено
            boolean isTaken = ticketRepository.existsBySessionIdAndRowNumAndColNumAndStatusIn(
                    session.getId(), coord.row(), coord.col(),
                    List.of(TicketStatus.RESERVED, TicketStatus.PAID));

            if (isTaken) {
                throw new ValidationException("Seat [%d, %d] is not available".formatted(coord.row(), coord.col()));
            }

            SeatEntity seat = seatRepository.findByHallIdAndRowNumAndColNum(
                    session.getHall().getId(), coord.row(), coord.col())
                    .orElseThrow(() -> new ValidationException("Physical seat not found"));

            BigDecimal price = session.getBasePrice().multiply(seat.getSeatType().getCoef());
            totalAmount = totalAmount.add(price);

            TicketEntity ticket = ticketRepository
                    .findBySessionIdAndRowNumAndColNumAndStatus(session.getId(), coord.row(), coord.col(),
                            TicketStatus.CANCELLED)
                    .orElseGet(TicketEntity::new);

            // 3. Перезаписываем поля (для reused-билетов это обновит статус и привязку)
            ticket.setRowNum(coord.row());
            ticket.setColNum(coord.col());
            ticket.setSession(session);
            ticket.setStatus(TicketStatus.RESERVED);
            ticket.setBookedAt(Instant.now());
            ticket.setTicketCode(null); // Очищаем старый QR-код
            ticket.setFinalPrice(price);
            // ⚠️ Привязка к новому заказу произойдёт позже: tickets.add(ticket) + saveAll()
            tickets.add(ticket);
        }

        // 4. Создание заказа
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setCustomerEmail(user.getEmail());
        order.setTotalAmount(totalAmount);
        // order.setTotal(totalAmount); //TODO добавить когда будут скидки
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        orderRepository.save(order);

        // 5. Привязка билетов
        for (TicketEntity t : tickets)
            t.setOrder(order);
        ticketRepository.saveAll(tickets);

        return OrderRs.from(order);
    }

    @Transactional
    public OrderRs confirmPayment(Long orderId, Long userId) {
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException(OrderEntity.class, orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ValidationException("Order is not pending");
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Генерируем ticketCode для всех билетов заказа
        List<TicketEntity> tickets = ticketRepository.findByOrderId(orderId);
        for (TicketEntity t : tickets) {
            if (t.getStatus() == TicketStatus.RESERVED) {
                t.setStatus(TicketStatus.PAID);
                t.setTicketCode(UUID.randomUUID().toString());
            }
        }
        ticketRepository.saveAll(tickets);

        return OrderRs.from(order);
    }
}