package com.example.demo.api.userStatus;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserStatusRq(@NotBlank String name, @JsonProperty("is_active") boolean isActive,
        @JsonProperty("discount_size") @NotNull BigDecimal discountSize) {
}
