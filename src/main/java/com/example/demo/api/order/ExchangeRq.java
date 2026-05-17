package com.example.demo.api.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ExchangeRq(
                @JsonProperty("old_ticket_ids") @NotNull List<Long> oldTicketIds,
                @JsonProperty("new_session_id") @NotNull Long newSessionId,
                @NotNull List<SeatCoordRq> newSeats) {
}