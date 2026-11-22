package com.example.demo.api.userStatus;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDiscountRs(
        @JsonProperty("has_special_status") boolean hasSpecialStatus,

        @JsonProperty("status_discount_coef") BigDecimal statusDiscountCoef) {
}