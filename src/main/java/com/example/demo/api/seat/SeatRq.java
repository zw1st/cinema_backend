package com.example.demo.api.seat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeatRq(
        @NotNull @JsonProperty("hall_id") Long hallId,
        @NotNull @JsonProperty("seat_type_id") Long seatTypeId,
        @NotNull @Min(0) @JsonProperty("row_num") Integer rowNum,
        @NotNull @Min(0) @JsonProperty("col_num") Integer colNum) {
}