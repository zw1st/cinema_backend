package com.example.demo.api.order;

import jakarta.validation.constraints.NotNull;

public record SeatCoordRq(
        @NotNull Integer row,
        @NotNull Integer col) {
}