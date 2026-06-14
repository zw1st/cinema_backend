package com.example.demo.api.session;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.demo.api.movie.MovieShortRs;
import com.example.demo.entity.SessionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionShortRs(
        Long id,

        @JsonProperty("session_date") LocalDate sessionDate,

        @JsonProperty("session_start_time") LocalTime sessionStartTime,
        @JsonProperty("session_end_time") LocalTime sessionEndTime,

        MovieShortRs movie) {
    public static SessionShortRs from(SessionEntity session) {
        return new SessionShortRs(
                session.getId(),
                session.getDate(),
                session.getStartTime(),
                session.getEndTime(),
                MovieShortRs.from(session.getMovie()));
    }
}