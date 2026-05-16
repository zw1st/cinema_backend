package com.example.demo.api.order;

import com.example.demo.entity.OrderEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderRs(
        Long id,
        String status,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        @JsonProperty("created_at") Instant createdAt) {
    public static OrderRs from(OrderEntity order) {
        return new OrderRs(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt());
    }
}