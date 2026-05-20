package com.example.demo.api.giftCard;

import com.example.demo.entity.GiftCardEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record GiftCardRs(
        Long id,
        BigDecimal nominal,
        @JsonProperty("is_active") boolean isActive) {
    public static GiftCardRs from(GiftCardEntity e) {
        return new GiftCardRs(e.getId(), e.getNominal(), e.isActive());
    }
}