package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "movie")
public class MovieEntity extends BaseEntity {

    @Column(length = 100, nullable = false, unique = true)
    private String title;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    @Min(1)
    @Max(10)
    private double rating;

    @Column(nullable = false)
    @Min(1)
    @Max(6 * 60)
    private Short duration;

    @Column(name = "age_rating", nullable = false)
    private String ageRating;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "poster_image_url", length = 255)
    private String posterImageUrl;

    @Column(columnDefinition = "text")
    private String genres;

    @Column(columnDefinition = "text")
    private String actors;

    @Column(columnDefinition = "text")
    private String directors;

    public MovieEntity(String title, LocalDate releaseDate, boolean isActive, @Min(1) @Max(10) double rating,
            @Min(1) @Max(360) Short duration, String ageRating, String description, String posterImageUrl,
            String genres, String actors, String directors) {
        this();
        this.title = title;
        this.releaseDate = releaseDate;
        this.isActive = isActive;
        this.rating = rating;
        this.duration = duration;
        this.ageRating = ageRating;
        this.description = description;
        this.posterImageUrl = posterImageUrl;
        this.genres = genres;
        this.actors = actors;
        this.directors = directors;
    }

    public MovieEntity() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Short getDuration() {
        return duration;
    }

    public void setDuration(Short duration) {
        this.duration = duration;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterImageUrl() {
        return posterImageUrl;
    }

    public void setPosterImageUrl(String posterImageUrl) {
        this.posterImageUrl = posterImageUrl;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getDirectors() {
        return directors;
    }

    public void setDirectors(String directors) {
        this.directors = directors;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
