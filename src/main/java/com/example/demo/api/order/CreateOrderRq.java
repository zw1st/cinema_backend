package com.example.demo.api.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRq(
        @JsonProperty("session_id") @NotNull Long sessionId,
        @NotNull List<SeatCoordRq> seats) {
}
