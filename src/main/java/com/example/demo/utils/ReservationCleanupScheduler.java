package com.example.demo.utils;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.configuration.AppProperties;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.enumeration.OrderStatus;
import com.example.demo.entity.enumeration.TicketStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.service.OrderService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class ReservationCleanupScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final TicketRepository ticketRepository;
    private final AppProperties appProperties;

    public ReservationCleanupScheduler(OrderRepository orderRepository,
            TicketRepository ticketRepository,
            AppProperties appProperties, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.appProperties = appProperties;
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 60_000) // 🔹 Запуск каждую минуту
    @Transactional
    public void cleanupExpiredReservations() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(appProperties.getReservationTimer()));

        // 1. Находим зависшие PENDING-заказы
        List<OrderEntity> expiredOrders = orderRepository
                .findExpiredPendingOrders(OrderStatus.PENDING, threshold);
        if (expiredOrders.isEmpty())
            return;

        List<Long> orderIds = expiredOrders.stream().map(OrderEntity::getId).toList();
        List<TicketEntity> expiredTickets = ticketRepository
                .findByOrderIdsAndStatusIn(orderIds, List.of(TicketStatus.RESERVED, TicketStatus.EXCHANGING));

        // 3. Раздельная обработка по статусу
        for (TicketEntity t : expiredTickets) {
            if (t.getStatus() == TicketStatus.RESERVED) {
                // Обычная бронь → отменяем
                t.setStatus(TicketStatus.CANCELLED);
            } else if (t.getStatus() == TicketStatus.EXCHANGING) {
                // Билет из обмена → восстанавливаем в PAID (откат обмена)
                t.setStatus(TicketStatus.PAID);
            }
        }

        // 4. Отменяем заказы
        expiredOrders.forEach(o -> o.setStatus(OrderStatus.CANCELLED));

        expiredOrders.forEach(o -> o.setFinalPrice(BigDecimal.ZERO)); // Сбрасываем финальную цену заказа

        // 5. Фиксируем изменения
        orderRepository.saveAll(expiredOrders);
        ticketRepository.saveAll(expiredTickets);
    }
}