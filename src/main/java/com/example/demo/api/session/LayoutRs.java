package com.example.demo.api.session;

import java.util.List;

import com.example.demo.entity.HallEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LayoutRs(
        @JsonProperty("hall_id") Long hallId,
        @JsonProperty("hall_name") String hallName,
        @JsonProperty("total_rows") Integer totalRows,
        @JsonProperty("total_cols") Integer totalCols,
        List<SeatStatusDto> seats) {

    public static LayoutRs from(HallEntity hall, List<SeatStatusDto> seats) {
        return new LayoutRs(
                hall.getId(),
                hall.getName(),
                hall.getTotalRows(),
                hall.getTotalCols(),
                seats);
    }
}
