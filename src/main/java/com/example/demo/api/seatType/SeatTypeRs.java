package com.example.demo.api.seatType;

import com.example.demo.entity.SeatType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.StreamSupport;

public record SeatTypeRs(
        Long id,
        String name,
        @JsonProperty("additional_price") BigDecimal additionalPrice) {

    public static SeatTypeRs from(SeatType entity) {
        return new SeatTypeRs(
                entity.getId(),
                entity.getName(),
                entity.getAdditionalPrice());
    }

    public static List<SeatTypeRs> fromList(Iterable<SeatType> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(SeatTypeRs::from)
                .toList();
    }
}