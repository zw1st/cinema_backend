package com.example.demo.api.movie;

import com.example.demo.entity.MovieEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MovieShortRs(
        Long id,
        String title,
        @JsonProperty("poster_url") String posterUrl) {
    public static MovieShortRs from(MovieEntity movie) {
        return new MovieShortRs(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterImageUrl());
    }
}