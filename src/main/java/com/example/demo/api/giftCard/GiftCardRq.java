package com.example.demo.api.giftCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record GiftCardRq(
        @NotNull @DecimalMin("0.01") BigDecimal nominal,
        @JsonProperty("is_active") boolean isActive) {
}
