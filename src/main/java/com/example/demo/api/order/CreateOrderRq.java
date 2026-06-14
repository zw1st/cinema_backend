package com.example.demo.api.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRq(
        @JsonProperty("session_id") @NotNull Long sessionId,
        @JsonProperty("apply_status_discount") Boolean applyStatusDiscount, // true = применить статус
        @JsonProperty("applied_gift_card_id") Long appliedGiftCardId,
        @NotNull List<SeatCoordRq> seats) {
}
