package com.example.demo.api.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderRs createOrder(@Valid @RequestBody CreateOrderRq rq,
            @AuthenticationPrincipal Long userId) {
        return orderService.createOrder(rq, userId);
    }

    @PostMapping("/{id}/confirm")
    public OrderRs confirmPayment(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return orderService.confirmPayment(id, userId);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelWholeBooking(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        orderService.cancelBooking(orderId, null, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/tickets")
    public ResponseEntity<Void> cancelSpecificTickets(
            @PathVariable Long orderId,
            @RequestBody List<Long> ticketIds,
            @AuthenticationPrincipal Long userId) {
        orderService.cancelBooking(orderId, ticketIds, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/refund")
    public ResponseEntity<Void> refundWholeOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        orderService.refundTickets(orderId, null, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/refund/tickets")
    public ResponseEntity<Void> refundSpecificTickets(
            @PathVariable Long orderId,
            @RequestBody List<Long> ticketIds,
            @AuthenticationPrincipal Long userId) {
        orderService.refundTickets(orderId, ticketIds, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/exchange")
    public OrderRs exchangeTickets(
            @PathVariable Long orderId,
            @Valid @RequestBody ExchangeRq rq,
            @AuthenticationPrincipal Long userId) {
        return orderService.exchangeTickets(orderId, rq, userId);
    }
}