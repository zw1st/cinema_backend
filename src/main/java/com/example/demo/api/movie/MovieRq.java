package com.example.demo.api.movie;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovieRq(
        @NotBlank String title,
        @NotNull @JsonProperty("release_date") LocalDate releaseDate,
        @NotNull boolean isActive,
        @NotNull @Min(1) @Max(10) double rating,
        @NotNull @Min(1) @Max(6 * 60) Short duration,
        @NotNull @JsonProperty("age_rating") String ageRating,
        String description,
        @JsonProperty("poster_image_url") @Size(max = 255, min = 2) String posterImageUrl,
        String genres,
        String actors,
        String directors) {
}
