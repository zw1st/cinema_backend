package com.example.demo.api.review;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRq(
        @NotNull @Min(1) @Max(10) short grage,
        @NotNull LocalDate date,
        @NotBlank String author,
        @NotBlank String title,
        @NotBlank String text,
        @NotBlank @JsonProperty("movie_id") Long movieId) {

}
