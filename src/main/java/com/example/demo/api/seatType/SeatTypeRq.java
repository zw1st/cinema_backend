package com.example.demo.api.seatType;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeatTypeRq(
                @NotBlank @Size(max = 50) String name,
                @NotNull @DecimalMin("0.0") BigDecimal coef) {
}