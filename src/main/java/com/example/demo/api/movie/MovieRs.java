package com.example.demo.api.movie;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.entity.MovieEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MovieRs(
        Long id,
        String title,
        @JsonProperty("release_date") LocalDate releaseDate,
        @JsonProperty("is_active") boolean isActive,
        double rating,
        Short duration,
        @JsonProperty("age_rating") String ageRating,
        String description,
        @JsonProperty("poster_image_url") String posterImageUrl,
        String genres,
        String actors,
        String directors) {

    public static MovieRs from(MovieEntity entity) {
        return new MovieRs(entity.id, entity.getTitle(), entity.getReleaseDate(), entity.isActive(), entity.getRating(),
                entity.getDuration(),
                entity.getAgeRating(), entity.getDescription(), entity.getPosterImageUrl(), entity.getGenres(),
                entity.getActors(), entity.getDirectors());
    }

    public static List<MovieRs> fromList(Iterable<MovieEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(MovieRs::from)
                .toList();
    }
}
