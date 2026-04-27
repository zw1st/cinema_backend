package com.example.demo.api.review;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.entity.ReviewEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ReviewRs(
        short grade,
        LocalDate date,
        String author,
        String title,
        String text,
        @JsonProperty("movie_id") Long movieId) {

    public static ReviewRs from(ReviewEntity entity) {
        return new ReviewRs(
                entity.getGrade(),
                entity.getDate(),
                entity.getAuthor(),
                entity.getTitle(),
                entity.getText(),
                entity.getMovie().getId());
    }

    public static List<ReviewRs> fromList(Iterable<ReviewEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(ReviewRs::from)
                .toList();
    }

}
