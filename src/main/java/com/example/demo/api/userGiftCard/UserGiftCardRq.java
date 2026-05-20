package com.example.demo.api.userGiftCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record UserGiftCardRq(
        @JsonProperty("giftcard_id") @NotNull Long giftcardId,
        @JsonProperty("recipient_email") @Email @NotBlank String recipientEmail) {
}