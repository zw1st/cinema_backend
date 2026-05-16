package com.example.demo.api.order;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
}