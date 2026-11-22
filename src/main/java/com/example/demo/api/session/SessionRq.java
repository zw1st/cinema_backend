package com.example.demo.api.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record SessionRq(
                @NotNull @JsonProperty("movie_id") Long movieId,
                @NotNull @JsonProperty("hall_id") Long hallId,
                @NotNull LocalDate date,
                @NotNull @JsonProperty("start_time") LocalTime startTime,
                @NotNull @DecimalMin("0.0") @JsonProperty("base_price") BigDecimal basePrice) {
}