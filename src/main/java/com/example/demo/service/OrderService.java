package com.example.demo.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.order.CreateOrderRq;
import com.example.demo.api.order.ExchangeRq;
import com.example.demo.api.order.OrderRs;
import com.example.demo.api.order.SeatCoordRq;
import com.example.demo.configuration.AppProperties;
import com.example.demo.entity.GiftCardEntity;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SessionEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.UserGiftCardEntity;
import com.example.demo.entity.enumeration.DiscountType;
import com.example.demo.entity.enumeration.GiftCardStatus;
import com.example.demo.entity.enumeration.OrderStatus;
import com.example.demo.entity.enumeration.TicketStatus;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.GiftCardRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.SessionRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserGiftCardRepository;
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
    private final UserGiftCardRepository userGiftCardRepository;

    public OrderService(OrderRepository orderRepository, TicketRepository ticketRepository,
            SessionRepository sessionRepository, SeatRepository seatRepository,
            UserRepository userRepository, AppProperties appProperties,
            SpecialStatusRequestService specialStatusRequestService,
            UserGiftCardService userGiftCardService, UserGiftCardRepository userGiftCardRepository) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.sessionRepository = sessionRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.specialStatusRequestService = specialStatusRequestService;
        this.userGiftCardService = userGiftCardService;
        this.appProperties = appProperties;
        this.userGiftCardRepository = userGiftCardRepository;
    }

    // DONE
    @Transactional
    public OrderRs createOrder(CreateOrderRq rq, Long userId) {

        boolean wantsStatus = rq.applyStatusDiscount() != null && rq.applyStatusDiscount();
        boolean wantsCard = rq.appliedGiftCardId() != null;

        if (wantsStatus && wantsCard) {
            throw new ValidationException("Cannot apply both status discount and gift card. Please choose one.");
        }

        DiscountType discountType = wantsStatus ? DiscountType.SPECIAL_STATUS
                : wantsCard ? DiscountType.GIFT_CARD
                        : DiscountType.NONE;

        // Пользователь
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
            ticket.setTicketCode(null);
            ticket.setFinalPrice(price);
            // ⚠️ Привязка к новому заказу произойдёт позже: tickets.add(ticket) + saveAll()
            tickets.add(ticket);
        }

        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setCustomerEmail(user.getEmail());
        order.setTotalAmount(totalAmount);
        order.setDiscountType(discountType);

        if (discountType == DiscountType.SPECIAL_STATUS) {
            BigDecimal coef = specialStatusRequestService.getMaxDiscountCoef(userId);
            order.setStatusDiscountCoef(coef);
        } else if (discountType == DiscountType.GIFT_CARD) {
            // Валидация и привязка карты
            UserGiftCardEntity card = userGiftCardService.validateCard(rq.appliedGiftCardId(), userId);
            boolean isCardReserved = orderRepository.existsByAppliedGiftCardIdAndStatus(
                    rq.appliedGiftCardId(), OrderStatus.PENDING);
            if (isCardReserved) {
                throw new ValidationException("Gift card is already reserved in another pending order");
            }
            order.setAppliedGiftCardId(card.getId());
        }

        // 🔹 Единый вызов расчёта
        order.setFinalPrice(calculateFinalPrice(totalAmount, order));

        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        orderRepository.save(order);

        // 5. Привязка билетов
        for (TicketEntity t : tickets) {
            t.setOrder(order);
        }
        ticketRepository.saveAll(tickets);

        return OrderRs.from(order, tickets

        );
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

        // 5. 🔹 Логика обмена: пересчёт старого заказа
        if (order.getExchangeFromOrderId() != null) {
            Long oldOrderId = order.getExchangeFromOrderId();

            // 5.1. Находим билеты, которые были заблокированы для обмена
            List<TicketEntity> exchangingTickets = ticketRepository.findByOrderIdAndStatus(
                    oldOrderId, TicketStatus.EXCHANGING);

            // 5.2. Переводим их в CANCELLED (обмен завершён успешно)
            exchangingTickets.forEach(t -> t.setStatus(TicketStatus.CANCELLED));
            ticketRepository.saveAll(exchangingTickets);

            // 5.3. Загружаем старый заказ для пересчёта
            OrderEntity oldOrder = orderRepository.findById(oldOrderId)
                    .orElseThrow(() -> new NotFoundException("Original order not found"));

            // 5.4. 🔹 ПЕРЕРАСЧЁТ СТАРОГО ЗАКАЗА (новая логика)
            // Считаем сумму базовых цен ОСТАВШИХСЯ активных билетов (статус PAID)
            List<TicketEntity> remainingTickets = ticketRepository.findByOrderIdAndStatus(
                    oldOrderId, TicketStatus.PAID);

            BigDecimal recalculatedTotal = remainingTickets.stream()
                    .map(TicketEntity::getFinalPrice) // базовая цена билета
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Обновляем totalAmount
            oldOrder.setTotalAmount(recalculatedTotal);

            // 🔹 Пересчитываем finalPrice через ОБЩИЙ метод с сохранённым типом скидки
            oldOrder.setFinalPrice(calculateFinalPrice(recalculatedTotal, oldOrder));

            // 5.5. Если активных билетов не осталось → отменяем заказ
            if (remainingTickets.isEmpty()) {
                oldOrder.setStatus(OrderStatus.CANCELLED);
            }

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

        // 2. Если отменяется заявка на обмен → восстанавливаем старые билеты
        if (order.getExchangeFromOrderId() != null) {
            List<TicketEntity> exchangingTickets = ticketRepository.findByOrderIdAndStatus(
                    order.getExchangeFromOrderId(), TicketStatus.EXCHANGING);

            exchangingTickets.forEach(t -> t.setStatus(TicketStatus.PAID));
            ticketRepository.saveAll(exchangingTickets);

            OrderEntity oldOrder = orderRepository.findById(order.getExchangeFromOrderId())
                    .orElseThrow(() -> new NotFoundException("Original order not found"));

            // Проверяем, есть ли в старом заказе билеты со статусом PAID или EXCHANGING
            boolean hasActiveTickets = !ticketRepository.findByOrderIdAndStatusIn(
                    oldOrder.getId(), List.of(TicketStatus.PAID, TicketStatus.EXCHANGING)).isEmpty();

            if (hasActiveTickets && oldOrder.getStatus() == OrderStatus.CANCELLED) {
                oldOrder.setStatus(OrderStatus.PAID);
                orderRepository.save(oldOrder);
            }

        }

        // 3. Определение списка билетов к отмене
        List<TicketEntity> ticketsToCancel;
        if (ticketIdsToCancel == null || ticketIdsToCancel.isEmpty()) {
            ticketsToCancel = ticketRepository.findByOrderIdAndStatus(orderId, TicketStatus.RESERVED);
        } else {
            ticketsToCancel = ticketRepository.findByOrderIdAndIdIn(orderId, ticketIdsToCancel);
        }

        if (ticketsToCancel.isEmpty()) {
            throw new ValidationException("No reserved tickets found to cancel");
        }

        // 4. Валидация и смена статуса
        for (TicketEntity t : ticketsToCancel) {
            if (t.getStatus() != TicketStatus.RESERVED) {
                throw new ValidationException("Ticket %d is not in RESERVED status".formatted(t.getId()));
            }
            t.setStatus(TicketStatus.CANCELLED);
        }
        ticketRepository.saveAll(ticketsToCancel);

        // 5. 🔹 ПЕРЕРАСЧЁТ ЦЕНЫ ЗАКАЗА (новая логика)
        // Считаем сумму базовых цен ОСТАВШИХСЯ билетов со статусом RESERVED
        List<TicketEntity> remainingTickets = ticketRepository.findByOrderIdAndStatus(orderId, TicketStatus.RESERVED);

        BigDecimal recalculatedTotal = remainingTickets.stream()
                .map(TicketEntity::getFinalPrice) // базовая цена билета
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Обновляем totalAmount
        order.setTotalAmount(recalculatedTotal);

        // 🔹 Пересчитываем finalPrice через ОБЩИЙ метод с сохранённым типом скидки
        order.setFinalPrice(calculateFinalPrice(recalculatedTotal, order));

        // 6. 🔹 Освобождение подарочной карты при полной отмене
        if (order.getAppliedGiftCardId() != null && remainingTickets.isEmpty()) {
            order.setAppliedGiftCardId(null);
            // Карта остаётся в статусе purchased, так как активация происходит только в
            // confirmPayment
        }

        // 7. Обновление статуса заказа, если броней не осталось
        if (remainingTickets.isEmpty()) {
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
            // Валидация: все билеты должны принадлежать этому заказу
            for (TicketEntity t : ticketsToRefund) {
                if (!t.getOrder().getId().equals(orderId)) {
                    throw new ValidationException(
                            "Ticket %d does not belong to order %d".formatted(t.getId(), orderId));
                }
            }
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
        List<TicketEntity> remainingTickets = ticketRepository.findByOrderIdAndStatusIn(orderId,
                List.of(TicketStatus.PAID, TicketStatus.SCANNED));

        BigDecimal recalculatedTotal = remainingTickets.stream()
                .map(TicketEntity::getFinalPrice) // базовая цена билета
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Обновляем totalAmount
        order.setTotalAmount(recalculatedTotal);

        // 🔹 Пересчитываем finalPrice через ОБЩИЙ метод с сохранённым типом скидки
        order.setFinalPrice(calculateFinalPrice(recalculatedTotal, order));

        // 5. 🔹 Обработка подарочной карты при полном возврате
        if (order.getAppliedGiftCardId() != null && remainingTickets.isEmpty()) {
            // Все билеты возвращены → возвращаем карту в статус purchased через сервис
            userGiftCardService.reactivateCard(order.getAppliedGiftCardId());
            order.setAppliedGiftCardId(null);
        }
        // Если остались оплаченные билеты → карта остаётся activated (частичный
        // возврат)

        // 6. Обновление статуса заказа, если оплаченных билетов не осталось
        if (remainingTickets.isEmpty()) {
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

            TicketEntity nt = ticketRepository
                    .findBySessionIdAndRowNumAndColNumAndStatus(
                            newSession.getId(), coord.row(), coord.col(), TicketStatus.CANCELLED)
                    .orElseGet(TicketEntity::new);

            nt.setRowNum(coord.row());
            nt.setColNum(coord.col());
            nt.setSession(newSession);
            nt.setFinalPrice(price);
            nt.setStatus(TicketStatus.RESERVED); // Двухфазная модель: сначала RESERVED
            nt.setBookedAt(Instant.now());
            nt.setTicketCode(null);
            newTickets.add(nt);
        }

        boolean wantsStatus = rq.applyStatusDiscount() != null && rq.applyStatusDiscount();
        boolean wantsCard = rq.appliedGiftCardId() != null;

        if (wantsStatus && wantsCard) {
            throw new ValidationException("Cannot apply both status discount and gift card to new order");
        }

        DiscountType newDiscountType = wantsStatus ? DiscountType.SPECIAL_STATUS
                : wantsCard ? DiscountType.GIFT_CARD
                        : DiscountType.NONE;

        // Создаём новый заказ и настраиваем поля скидки
        OrderEntity newOrder = new OrderEntity();
        newOrder.setUser(oldOrder.getUser());
        newOrder.setCustomerEmail(oldOrder.getCustomerEmail());
        newOrder.setTotalAmount(newTotal);
        newOrder.setDiscountType(newDiscountType);

        if (newDiscountType == DiscountType.SPECIAL_STATUS) {
            BigDecimal coef = specialStatusRequestService.getMaxDiscountCoef(userId);
            newOrder.setStatusDiscountCoef(coef);
        } else if (newDiscountType == DiscountType.GIFT_CARD) {
            UserGiftCardEntity card = userGiftCardService.validateCard(rq.appliedGiftCardId(), userId);
            boolean isCardReserved = orderRepository.existsByAppliedGiftCardIdAndStatus(
                    rq.appliedGiftCardId(), OrderStatus.PENDING);
            if (isCardReserved) {
                throw new ValidationException("Gift card is already reserved in another pending order");
            }
            newOrder.setAppliedGiftCardId(card.getId());
        }

        // 🔹 Единый расчёт финальной цены для нового заказа
        newOrder.setFinalPrice(calculateFinalPrice(newTotal, newOrder));

        newOrder.setExchangeFromOrderId(oldOrderId);
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setCreatedAt(Instant.now());
        orderRepository.save(newOrder);

        // 🔹 7. Блокировка старых билетов
        oldTickets.forEach(t -> t.setStatus(TicketStatus.EXCHANGING));
        ticketRepository.saveAll(oldTickets);

        // 🔹 8. ПЕРЕРАСЧЁТ СТАРОГО ЗАКАЗА (ключевое изменение)
        // Считаем сумму базовых цен ОСТАВШИХСЯ активных билетов (статус PAID)
        List<TicketEntity> remainingOldTickets = ticketRepository.findByOrderIdAndStatus(
                oldOrderId, TicketStatus.PAID);

        BigDecimal recalculatedTotal = remainingOldTickets.stream()
                .map(TicketEntity::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Обновляем totalAmount
        oldOrder.setTotalAmount(recalculatedTotal);

        // 🔹 Пересчитываем finalPrice через ОБЩИЙ метод с сохранённым ТИПОМ СКИДКИ
        // старого заказа
        // Это гарантирует: если старая скидка была картой, она применится к новому
        // totalAmount
        oldOrder.setFinalPrice(calculateFinalPrice(recalculatedTotal, oldOrder));

        // Если активных билетов не осталось → отменяем заказ
        if (remainingOldTickets.isEmpty()) {
            oldOrder.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(oldOrder);

        // 9. Привязка новых билетов к новому заказу
        newTickets.forEach(t -> t.setOrder(newOrder));
        ticketRepository.saveAll(newTickets);

        // 10. Возврат DTO нового заказа
        return OrderRs.from(newOrder, newTickets);
    }

    public List<OrderRs> getOrdersForUser(Long userId, boolean withoutCancelled) {
        List<OrderEntity> orders = orderRepository.findByUserIdWithTickets(userId);
        return orders.stream()
                .map(order -> {
                    List<TicketEntity> ticketsToMap = order.getTickets();

                    // 🔹 Фильтрация только если передан флаг и билеты не null
                    if (withoutCancelled && ticketsToMap != null) {
                        ticketsToMap = ticketsToMap.stream()
                                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                                .toList();
                    }

                    // Маппим уже отфильтрованный (или оригинальный) список
                    return OrderRs.from(order, ticketsToMap);
                })
                .toList();
    }

    public void markTicketAsScanned(Long orderId, Long ticketId, Long userId) {
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException(OrderEntity.class, orderId));

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException(TicketEntity.class, ticketId));

        if (!ticket.getOrder().getId().equals(orderId)) {
            throw new ValidationException("Ticket does not belong to the specified order");
        }
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new ValidationException("Only PAID tickets can be marked as scanned");
        }

        ticket.setStatus(TicketStatus.SCANNED);
        ticketRepository.save(ticket);
    }

    public BigDecimal calculateFinalPrice(BigDecimal totalAmount, OrderEntity order) {
        return switch (order.getDiscountType()) {
            case SPECIAL_STATUS -> {
                // Скидка статуса: процент от базовой цены
                yield totalAmount.multiply(order.getStatusDiscountCoef());
            }
            case GIFT_CARD -> {
                // Подарочная карта: вычитаем номинал, защита от отрицательной цены
                if (order.getAppliedGiftCardId() == null) {
                    throw new ValidationException("Gift card discount selected but no card ID provided");
                }
                // Номинал берём из справочника (не из сущности карты, чтобы избежать ленивой
                // загрузки)
                BigDecimal nominal = userGiftCardRepository.findById(order.getAppliedGiftCardId())
                        .orElseThrow(
                                () -> new NotFoundException(UserGiftCardEntity.class, order.getAppliedGiftCardId()))
                        .getGiftCard().getNominal();

                BigDecimal result = totalAmount.subtract(nominal);
                yield result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
            }
            case NONE -> totalAmount; // Без скидки
        };
    }
}