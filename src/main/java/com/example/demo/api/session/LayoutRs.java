package com.example.demo.api.session;

import java.util.List;

import com.example.demo.entity.HallEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LayoutRs(
        @JsonProperty("hall_id") Long hallId,
        @JsonProperty("hall_name") String hallName,
        @JsonProperty("total_rows") Integer totalRows,
        @JsonProperty("total_cols") Integer totalCols,
        @JsonProperty("matrix_scheme") String[][] matrixScheme, // Схема зала: 2D-список с названиями типов мест
        List<SeatStatusDto> seats) {

    public static LayoutRs from(HallEntity hall, List<SeatStatusDto> seats, String[][] matrixScheme) {
        return new LayoutRs(
                hall.getId(),
                hall.getName(),
                hall.getTotalRows(),
                hall.getTotalCols(),
                matrixScheme,
                seats);
    }
}
