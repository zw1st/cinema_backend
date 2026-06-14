package com.example.demo.api.userGiftCard;

import com.example.demo.entity.UserGiftCardEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserGiftCardRs(
        Long id,
        BigDecimal nominal,
        @JsonProperty("buyer_id") Long buyerId,
        @JsonProperty("owner_id") Long ownerId,
        @JsonProperty("recipient_email") String recipientEmail,
        @JsonProperty("sender_email") String senderEmail,
        @JsonProperty("purchased_at") LocalDateTime purchasedAt,
        @JsonProperty("expire_date") LocalDate expireDate,
        String status,
        @JsonProperty("applied_to_order_id") Long appliedToOrderId) {
    public static UserGiftCardRs from(UserGiftCardEntity e) {
        return new UserGiftCardRs(
                e.getId(),
                e.getGiftCard().getNominal(),
                e.getBuyer().getId(),
                e.getOwner() != null ? e.getOwner().getId() : null,
                e.getRecipientEmail(),
                e.getSenderEmail(),
                e.getPurchasedAt(),
                e.getExpireDate(),
                e.getStatus().name(),
                e.getAppliedToOrder() != null ? e.getAppliedToOrder().getId() : null);
    }
}
