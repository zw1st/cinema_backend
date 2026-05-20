package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.order.CreateOrderRq;
import com.example.demo.api.order.ExchangeRq;
import com.example.demo.api.order.OrderRs;
import com.example.demo.api.order.SeatCoordRq;
import com.example.demo.configuration.AppProperties;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SessionEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserGiftCardEntity;
import com.example.demo.entity.enumeration.GiftCardStatus;
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
    private final SpecialStatusRequestService specialStatusRequestService;
    private final UserGiftCardService userGiftCardService;
    private final AppProperties appProperties;

    public OrderService(OrderRepository orderRepository, TicketRepository ticketRepository,
            SessionRepository sessionRepository, SeatRepository seatRepository,
            UserRepository userRepository, AppProperties appProperties,
            SpecialStatusRequestService specialStatusRequestService,
            UserGiftCardService userGiftCardService) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.sessionRepository = sessionRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.specialStatusRequestService = specialStatusRequestService;
        this.userGiftCardService = userGiftCardService;
        this.appProperties = appProperties;
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
                    List.of(TicketStatus.RESERVED, TicketStatus.PAID, TicketStatus.EXCHANGING));

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

        // Расчёт скидки по статусу
        BigDecimal statusCoef = specialStatusRequestService.getMaxDiscountCoef(userId); // 0.95 или 1.0
        BigDecimal priceAfterStatus = totalAmount.multiply(statusCoef);

        // Обработка подарочной карты
        BigDecimal giftDiscount = BigDecimal.ZERO;
        Long appliedCardId = null;

        if (rq.appliedGiftCardId() != null) {
            // Валидация карты: владелец, статус, срок
            UserGiftCardEntity card = userGiftCardService.validateCard(rq.appliedGiftCardId(), userId);

            // Проверка: не используется ли карта уже в другом PENDING-заказе
            boolean isCardReserved = orderRepository.existsByAppliedGiftCardId(rq.appliedGiftCardId());
            if (isCardReserved) {
                throw new ValidationException("Gift card is already reserved in another pending order");
            }

            giftDiscount = card.getGiftCard().getNominal();
            appliedCardId = card.getId();
        }

        BigDecimal finalPrice = priceAfterStatus.subtract(giftDiscount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO; // Излишек карты сгорает по ТЗ
        }

        OrderEntity order = new OrderEntity();
        // Привязка билетов
        for (TicketEntity t : tickets)
            t.setOrder(order);

        // Создание заказа
        order.setUser(user);
        order.setCustomerEmail(user.getEmail());
        order.setTotalAmount(totalAmount);
        order.setFinalPrice(finalPrice); // Итоговая сумма с учётом скидки по статусу и подарочной карты
        order.setAppliedGiftCardId(appliedCardId);
        order.setStatus(OrderStatus.PENDING);
        order.setStatusDiscountCoef(statusCoef);
        order.setCreatedAt(Instant.now());

        orderRepository.save(order);
        ticketRepository.saveAll(tickets);

        return OrderRs.from(order, tickets);
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

        // Активация подарочной карты (если применялась)
        if (order.getAppliedGiftCardId() != null) {
            userGiftCardService.activateCard(order.getAppliedGiftCardId(), orderId);
        }

        if (order.getExchangeFromOrderId() != null) {
            Long oldOrderId = order.getExchangeFromOrderId();
            List<TicketEntity> exchangingTickets = ticketRepository.findByOrderIdAndStatus(oldOrderId,
                    TicketStatus.EXCHANGING);

            // Отменяем заблокированные билеты
            exchangingTickets.forEach(t -> t.setStatus(TicketStatus.CANCELLED));
            ticketRepository.saveAll(exchangingTickets);

            OrderEntity oldOrder = orderRepository.findById(oldOrderId)
                    .orElseThrow(() -> new NotFoundException("Original order not found"));
            BigDecimal refundAmount = order.getAdjustmentAmount().abs();
            oldOrder.setTotalAmount(oldOrder.getTotalAmount().subtract(refundAmount));
            oldOrder.setFinalPrice(oldOrder.getFinalPrice().subtract(refundAmount));

            boolean hasPaidLeft = !ticketRepository.findByOrderIdAndStatus(oldOrderId, TicketStatus.PAID).isEmpty();
            if (!hasPaidLeft)
                oldOrder.setStatus(OrderStatus.CANCELLED);

            orderRepository.save(oldOrder);
        }

        return OrderRs.from(order, tickets);
    }

    @Transactional
    public void cancelBooking(Long orderId, List<Long> ticketIdsToCancel, Long userId) {
        // 1. Проверка владения и статуса заказа
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException(OrderEntity.class, orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ValidationException("Can only cancel PENDING bookings");
        }

        // Если отменяется заявка на обмен → восстанавливаем старые билеты
        if (order.getExchangeFromOrderId() != null) {
            List<TicketEntity> exchangingTickets = ticketRepository.findByOrderIdAndStatus(
                    order.getExchangeFromOrderId(), TicketStatus.EXCHANGING);
            exchangingTickets.forEach(t -> t.setStatus(TicketStatus.PAID));
            ticketRepository.saveAll(exchangingTickets);
        }

        // 2. Определение списка билетов к отмене
        List<TicketEntity> ticketsToCancel;
        if (ticketIdsToCancel == null || ticketIdsToCancel.isEmpty()) {
            // Если список пуст → отменяем все резервы заказа
            ticketsToCancel = ticketRepository.findByOrderIdAndStatus(orderId, TicketStatus.RESERVED);
        } else {
            ticketsToCancel = ticketRepository.findByOrderIdAndIdIn(orderId, ticketIdsToCancel);
        }

        if (ticketsToCancel.isEmpty()) {
            throw new ValidationException("No reserved tickets found to cancel");
        }

        // 3. Валидация и смена статуса
        for (TicketEntity t : ticketsToCancel) {
            if (t.getStatus() != TicketStatus.RESERVED) {
                throw new ValidationException("Ticket %d is not in RESERVED status".formatted(t.getId()));
            }
            t.setStatus(TicketStatus.CANCELLED);
        }
        ticketRepository.saveAll(ticketsToCancel);

        // 4. Пересчёт суммы заказа
        BigDecimal deducted = ticketsToCancel.stream()
                .map(TicketEntity::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(order.getTotalAmount().subtract(deducted));
        order.setFinalPrice(order.getFinalPrice().subtract(deducted));

        if (order.getAppliedGiftCardId() != null) {
            order.setAppliedGiftCardId(null);
            // Карта остаётся в статусе purchased, так как активация происходит только в
            // confirmPayment
        }

        // 5. Обновление статуса заказа, если броней не осталось
        boolean hasActiveReservations = !ticketRepository.findByOrderIdAndStatus(orderId, TicketStatus.RESERVED)
                .isEmpty();
        if (!hasActiveReservations) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(order);
    }

    @Transactional
    public void refundTickets(Long orderId, List<Long> ticketIdsToRefund, Long userId) {
        // 1. Проверка владения и статуса заказа
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException(OrderEntity.class, orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new ValidationException("Can only refund PAID orders");
        }

        // 2. Определение списка билетов к возврату
        List<TicketEntity> ticketsToRefund;
        if (ticketIdsToRefund == null || ticketIdsToRefund.isEmpty()) {
            // Если список пуст → возвращаем все оплаченные билеты заказа
            ticketsToRefund = ticketRepository.findByOrderIdAndStatus(orderId, TicketStatus.PAID);
        } else {
            ticketsToRefund = ticketRepository.findByIdIn(ticketIdsToRefund);
        }

        if (ticketsToRefund.isEmpty()) {
            throw new ValidationException("No paid tickets found to refund");
        }

        // 3. Валидация каждого билета
        LocalDateTime now = LocalDateTime.now(appProperties.getTimezone());
        for (TicketEntity t : ticketsToRefund) {
            if (t.getStatus() == TicketStatus.EXCHANGING) {
                throw new ValidationException("Ticket %d is currently part of a pending exchange".formatted(t.getId()));
            }
            if (t.getStatus() != TicketStatus.PAID) {
                throw new ValidationException("Ticket %d is not PAID".formatted(t.getId()));
            }
            // Проверка: до начала сеанса >= noRefundBeforeSession минут
            LocalDateTime sessionStart = LocalDateTime.of(
                    t.getSession().getDate(),
                    t.getSession().getStartTime());
            LocalDateTime cutoff = sessionStart.minusMinutes(appProperties.getNoRefundBeforeSession());

            if (now.isAfter(cutoff)) {
                throw new ValidationException("Refund denied: less than %d minutes before session"
                        .formatted(appProperties.getNoRefundBeforeSession()));
            }
            t.setStatus(TicketStatus.CANCELLED);
        }
        ticketRepository.saveAll(ticketsToRefund);

        // 4. Пересчёт суммы заказа
        BigDecimal refunded = ticketsToRefund.stream()
                .map(t -> t.getFinalPrice().multiply(order.getStatusDiscountCoef()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(order.getTotalAmount().subtract(refunded));
        order.setFinalPrice(order.getFinalPrice().subtract(refunded));

        // Обработка подарочной карты при возврате
        if (order.getAppliedGiftCardId() != null) {
            boolean hasPaidTicketsLeft = !ticketRepository.findByOrderIdAndStatus(orderId, TicketStatus.PAID).isEmpty();

            if (!hasPaidTicketsLeft) {
                // Все билеты возвращены → возвращаем карту через сервис
                userGiftCardService.reactivateCard(order.getAppliedGiftCardId());
                order.setAppliedGiftCardId(null);
            }
            // Если остались оплаченные билеты → карта остаётся activated (частичный
            // возврат)
        }

        // 5. Обновление статуса заказа, если оплаченных билетов не осталось
        boolean hasPaidTickets = !ticketRepository.findByOrderIdAndStatus(orderId, TicketStatus.PAID).isEmpty();
        if (!hasPaidTickets) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(order);
    }

    @Transactional
    public OrderRs exchangeTickets(Long oldOrderId, ExchangeRq rq, Long userId) {
        // 1. Валидация старого заказа
        OrderEntity oldOrder = orderRepository.findByIdAndUserId(oldOrderId, userId)
                .orElseThrow(() -> new NotFoundException(OrderEntity.class, oldOrderId));
        if (oldOrder.getStatus() != OrderStatus.PAID) {
            throw new ValidationException("Exchange allowed only for PAID orders");
        }

        // 2. Загрузка и валидация старых билетов
        List<TicketEntity> oldTickets = ticketRepository.findByIdIn(rq.oldTicketIds());
        if (oldTickets.isEmpty() || oldTickets.size() != rq.oldTicketIds().size()) {
            throw new ValidationException("Some old tickets not found");
        }

        LocalDateTime now = LocalDateTime.now(appProperties.getTimezone());
        BigDecimal oldRefund = BigDecimal.ZERO;

        for (TicketEntity t : oldTickets) {
            if (!t.getOrder().getId().equals(oldOrderId)) {
                throw new ValidationException("Ticket %d belongs to another order".formatted(t.getId()));
            }
            if (t.getStatus() == TicketStatus.EXCHANGING) {
                throw new ValidationException("Ticket %d is currently part of a pending exchange".formatted(t.getId()));
            }
            if (t.getStatus() != TicketStatus.PAID) {
                throw new ValidationException("Ticket %d is not PAID".formatted(t.getId()));
            }
            LocalDateTime cutoff = LocalDateTime.of(t.getSession().getDate(), t.getSession().getStartTime())
                    .minusMinutes(appProperties.getNoRefundBeforeSession());
            if (now.isAfter(cutoff)) {
                throw new ValidationException("Exchange denied: too close to session start");
            }
            oldRefund = oldRefund.add(t.getFinalPrice());
        }

        // 3. Валидация нового сеанса
        SessionEntity newSession = sessionRepository.findById(rq.newSessionId())
                .orElseThrow(() -> new NotFoundException(SessionEntity.class, rq.newSessionId()));
        LocalDateTime newCutoff = LocalDateTime.of(newSession.getDate(), newSession.getStartTime())
                .minusMinutes(appProperties.getNoRefundBeforeSession());
        if (now.isAfter(newCutoff)) {
            throw new ValidationException("Exchange denied: new session is too close");
        }

        // 4. Лимит мест
        if (rq.newSeats().size() > appProperties.getTicketPerOrder()) {
            throw new ValidationException("Max %d seats per transaction".formatted(appProperties.getTicketPerOrder()));
        }

        // 5. Проверка новых мест + расчёт новой стоимости
        BigDecimal newTotal = BigDecimal.ZERO;
        List<TicketEntity> newTickets = new ArrayList<>();

        for (SeatCoordRq coord : rq.newSeats()) {
            boolean isTaken = ticketRepository.existsBySessionIdAndRowNumAndColNumAndStatusIn(
                    newSession.getId(), coord.row(), coord.col(),
                    List.of(TicketStatus.RESERVED, TicketStatus.PAID, TicketStatus.EXCHANGING));
            if (isTaken) {
                throw new ValidationException("New seat [%d,%d] is already taken".formatted(coord.row(), coord.col()));
            }

            SeatEntity seat = seatRepository.findByHallIdAndRowNumAndColNum(
                    newSession.getHall().getId(), coord.row(), coord.col())
                    .orElseThrow(() -> new ValidationException("Physical seat not found"));

            BigDecimal price = newSession.getBasePrice().multiply(seat.getSeatType().getCoef());
            newTotal = newTotal.add(price);

            TicketEntity nt = new TicketEntity();
            nt.setRowNum(coord.row());
            nt.setColNum(coord.col());
            nt.setSession(newSession);
            nt.setFinalPrice(price);
            nt.setStatus(TicketStatus.RESERVED); // Двухфазная модель: сначала RESERVED
            nt.setBookedAt(Instant.now());
            newTickets.add(nt);
        }

        BigDecimal statusCoef = specialStatusRequestService.getMaxDiscountCoef(userId);
        BigDecimal priceAfterStatus = newTotal.multiply(statusCoef);

        BigDecimal giftDiscount = BigDecimal.ZERO;
        Long newAppliedCardId = null;

        if (rq.appliedGiftCardId() != null) {
            // Валидация через сервис
            UserGiftCardEntity card = userGiftCardService.validateCard(rq.appliedGiftCardId(), userId);

            // Проверка: не используется ли карта уже в другом PENDING-заказе
            boolean isCardReserved = orderRepository.existsByAppliedGiftCardId(rq.appliedGiftCardId());
            if (isCardReserved) {
                throw new ValidationException("Gift card is already reserved in another pending order");
            }

            giftDiscount = card.getGiftCard().getNominal();
            newAppliedCardId = card.getId();
        }

        BigDecimal finalPrice = priceAfterStatus.subtract(giftDiscount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO; // Излишек карты сгорает по ТЗ
        }

        oldTickets.forEach(t -> t.setStatus(TicketStatus.EXCHANGING));
        ticketRepository.saveAll(oldTickets);

        // 7. Обновление старого заказа
        oldOrder.setTotalAmount(oldOrder.getTotalAmount().subtract(oldRefund));
        oldOrder.setFinalPrice(oldOrder.getFinalPrice().subtract(oldRefund));

        boolean hasPaidLeft = !ticketRepository.findByOrderIdAndStatus(oldOrderId, TicketStatus.PAID).isEmpty();
        if (!hasPaidLeft) {
            oldOrder.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(oldOrder);

        // 8. Создание нового заказа
        OrderEntity newOrder = new OrderEntity();
        newOrder.setUser(oldOrder.getUser());
        newOrder.setCustomerEmail(oldOrder.getCustomerEmail());
        newOrder.setTotalAmount(newTotal); // Base price новых билетов
        newOrder.setFinalPrice(finalPrice); // Итог к оплате
        newOrder.setStatusDiscountCoef(statusCoef); // Скидка статуса
        newOrder.setAppliedGiftCardId(newAppliedCardId); // Новая карта (или null)
        newOrder.setAdjustmentAmount(oldRefund.negate()); // Кредит за возврат (-800)
        newOrder.setExchangeFromOrderId(oldOrderId); // Связь со старым заказом
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setCreatedAt(Instant.now());
        orderRepository.save(newOrder);

        // 9. Привязка новых билетов к новому заказу
        newTickets.forEach(t -> t.setOrder(newOrder));
        ticketRepository.saveAll(newTickets);

        // 10. Возврат DTO нового заказа (клиент увидит amountToPay = 160)
        return OrderRs.from(newOrder, newTickets);
    }

    public List<OrderRs> getOrdersForUser(Long userId) {
        List<OrderEntity> orders = orderRepository.findByUserIdWithTickets(userId);
        return orders.stream()
                .map(order -> OrderRs.from(order, order.getTickets()))
                .toList();
    }
}