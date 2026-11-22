package com.example.demo.api.session;

import com.example.demo.entity.SessionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.StreamSupport;

public record SessionRs(
        Long id,
        @JsonProperty("movie_id") Long movieId,
        @JsonProperty("hall_id") Long hallId,
        LocalDate date,
        @JsonProperty("start_time") LocalTime startTime,
        @JsonProperty("end_time") LocalTime endTime,
        @JsonProperty("base_price") BigDecimal basePrice) {

    public static SessionRs from(SessionEntity entity) {
        return new SessionRs(
                entity.getId(),
                entity.getMovie().getId(),
                entity.getHall().getId(),
                entity.getDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getBasePrice());
    }

    public static List<SessionRs> fromList(Iterable<SessionEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(SessionRs::from)
                .toList();
    }
}