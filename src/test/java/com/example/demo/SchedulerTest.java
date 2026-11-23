package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.configuration.AppProperties;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.enumeration.OrderStatus;
import com.example.demo.entity.enumeration.TicketStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.utils.ReservationCleanupScheduler;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SchedulerTest {
    @Mock
    OrderRepository orderRepository;
    @Mock
    TicketRepository ticketRepository;
    @Mock
    AppProperties appProperties;

    @Captor
    ArgumentCaptor<Instant> thresholdCaptor;

    private ReservationCleanupScheduler scheduler;

    private static final long RESERVATION_MINUTES = 15;
    private static final Long ORDER_ID = 500L;

    @BeforeEach
    void setUp() {
        scheduler = new ReservationCleanupScheduler(
                orderRepository, ticketRepository, appProperties, null, null);
        when(appProperties.getReservationTimer()).thenReturn(15);
    }

    private OrderEntity pendingOrder() {
        OrderEntity order = new OrderEntity();
        order.setId(ORDER_ID);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000"));
        order.setFinalPrice(new BigDecimal("0"));
        order.setAppliedGiftCardId(100L);
        return order;
    }

    private TicketEntity ticket(Long id, TicketStatus status) {
        TicketEntity t = new TicketEntity();
        t.setId(id);
        t.setStatus(status);
        return t;
    }

    @Test
    void cleanupExpiredReservations_cancelsExpiredOrderAndReleasesItsTickets() {
        // given: заказ, который запрос признал просроченным, и два его забронированных
        // билета
        OrderEntity order = pendingOrder();
        List<OrderEntity> expiredOrders = List.of(order);
        List<TicketEntity> expiredTickets = List.of(
                ticket(1L, TicketStatus.RESERVED),
                ticket(2L, TicketStatus.RESERVED));

        when(orderRepository.findExpiredPendingOrders(eq(OrderStatus.PENDING), any(Instant.class)))
                .thenReturn(expiredOrders);
        when(ticketRepository.findByOrderIdsAndStatusIn(
                List.of(ORDER_ID), List.of(TicketStatus.RESERVED, TicketStatus.EXCHANGING)))
                .thenReturn(expiredTickets);

        Instant before = Instant.now();

        // when
        scheduler.cleanupExpiredReservations();

        Instant after = Instant.now();

        // then: порог = «сейчас минус срок брони»
        verify(orderRepository).findExpiredPendingOrders(eq(OrderStatus.PENDING), thresholdCaptor.capture());
        Instant threshold = thresholdCaptor.getValue();
        Duration timer = Duration.ofMinutes(RESERVATION_MINUTES);
        assertThat(threshold).isBetween(before.minus(timer), after.minus(timer));

        // then: заказ отменён, деньги и карта сброшены
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("0");
        assertThat(order.getFinalPrice()).isEqualByComparingTo("0");
        assertThat(order.getAppliedGiftCardId()).isNull();

        // then: места освобождены
        assertThat(expiredTickets).extracting(TicketEntity::getStatus)
                .containsOnly(TicketStatus.CANCELLED);

        // then: изменения сохранены
        verify(orderRepository).saveAll(expiredOrders);
        verify(ticketRepository).saveAll(expiredTickets);
    }

    @Test
    void cleanupExpiredReservations_whenNothingExpired_changesNothing() {
        when(orderRepository.findExpiredPendingOrders(eq(OrderStatus.PENDING), any(Instant.class)))
                .thenReturn(List.of());

        scheduler.cleanupExpiredReservations();

        verify(orderRepository, never()).saveAll(any());
        verifyNoInteractions(ticketRepository);
    }
}
