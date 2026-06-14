package com.example.demo.api.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ExchangeRq(
        @JsonProperty("old_ticket_ids") @NotNull List<Long> oldTicketIds,
        @JsonProperty("applied_gift_card_id") Long appliedGiftCardId,
        @JsonProperty("apply_status_discount") Boolean applyStatusDiscount,
        @JsonProperty("new_session_id") @NotNull Long newSessionId,
        @JsonProperty("new_seats") @NotNull List<SeatCoordRq> newSeats) {
}