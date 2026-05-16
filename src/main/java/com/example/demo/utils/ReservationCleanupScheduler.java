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

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class ReservationCleanupScheduler {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final AppProperties appProperties;

    public ReservationCleanupScheduler(OrderRepository orderRepository,
            TicketRepository ticketRepository,
            AppProperties appProperties) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.appProperties = appProperties;
    }

    @Scheduled(fixedDelay = 60_000) // 🔹 Запуск каждую минуту
    @Transactional
    public void cleanupExpiredReservations() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(appProperties.getReservationTimer()));

        // 1. Находим зависшие заказы
        List<OrderEntity> expiredOrders = orderRepository
                .findExpiredPendingOrders(OrderStatus.PENDING, threshold);
        if (expiredOrders.isEmpty())
            return;

        // 2. Находим связанные билеты
        List<Long> orderIds = expiredOrders.stream().map(OrderEntity::getId).toList();
        List<TicketEntity> expiredTickets = ticketRepository
                .findByOrderIdsAndStatus(orderIds, TicketStatus.RESERVED);

        // 3. Меняем статусы (JPA dirty checking сохранит изменения при commit)
        expiredOrders.forEach(o -> o.setStatus(OrderStatus.CANCELLED));
        expiredTickets.forEach(t -> t.setStatus(TicketStatus.CANCELLED));

        // 4. Фиксируем в БД
        orderRepository.saveAll(expiredOrders);
        ticketRepository.saveAll(expiredTickets);
    }
}