package com.example.demo.api.order;

import com.example.demo.api.ticket.TicketRs;
import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.TicketEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderRs(
        Long id,
        String status,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        @JsonProperty("status_discount_coef") BigDecimal statusDiscountCoef,
        @JsonProperty("applied_gift_card_id") Long appliedGiftCardId,
        @JsonProperty("final_price") BigDecimal finalPrice,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("exchange_from_order_id") Long exchangeFromOrderId,
        @JsonProperty("adjustment_amount") BigDecimal adjustmentAmount, List<TicketRs> tickets) {

    public static OrderRs from(OrderEntity order, List<TicketEntity> tickets) {
        List<TicketRs> ticketDtos = tickets != null
                ? tickets.stream().map(TicketRs::from).toList()
                : List.of();

        return new OrderRs(
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getStatusDiscountCoef(),
                order.getAppliedGiftCardId(),
                order.getFinalPrice(),
                order.getCreatedAt(),
                order.getExchangeFromOrderId(),
                order.getAdjustmentAmount(),
                ticketDtos);
    }
}