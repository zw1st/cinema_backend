package com.example.demo.api.seat;

import com.example.demo.entity.SeatEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.StreamSupport;

public record SeatRs(
        Long id,
        @JsonProperty("row_num") Integer rowNum,
        @JsonProperty("col_num") Integer colNum,
        @JsonProperty("seat_type_id") Long seatTypeId,
        @JsonProperty("hall_id") Long hallId) {

    public static SeatRs from(SeatEntity entity) {
        return new SeatRs(
                entity.getId(),
                entity.getRowNum(),
                entity.getColNum(),
                entity.getSeatType().getId(),
                entity.getHall().getId());
    }

    public static List<SeatRs> fromList(Iterable<SeatEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(SeatRs::from)
                .toList();
    }
}