package com.example.demo.api.userStatus;

import com.example.demo.entity.SpecialStatusEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record UserStatusRs(
        Long id,
        String name,
        @JsonProperty("is_active") boolean isActive,
        @JsonProperty("discount_size") BigDecimal discountSize) {
    public static UserStatusRs from(SpecialStatusEntity entity) {
        return new UserStatusRs(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.getDiscountSize());
    }
}