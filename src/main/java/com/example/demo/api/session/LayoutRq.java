package com.example.demo.api.session;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LayoutRq(
        @NotNull @JsonProperty("hall_id") Long hallId,
        @NotNull @Size(min = 1) @JsonProperty("seat_type_matrix") List<List<@NotNull @Min(0) Integer>> seatTypeMatrix) {
}