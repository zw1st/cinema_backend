package com.example.demo.api.hall;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HallRq(
                @NotBlank @Size(max = 50) String name,
                @NotNull @Min(1) @JsonProperty("total_rows") Integer totalRows,
                @NotNull @Min(1) @JsonProperty("total_cols") Integer totalCols) {
}
