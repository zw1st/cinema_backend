package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "review")
public class ReviewEntity extends BaseEntity {

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    @Min(1)
    @Max(10)
    private short grade;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @JoinColumn(name = "movie_id", nullable = false)
    @ManyToOne
    private MovieEntity movie;

    public ReviewEntity() {
        super();
    }

    public ReviewEntity(String author, short grade, LocalDate date, String title, String text, MovieEntity movie) {
        this.author = author;
        this.grade = grade;
        this.date = date;
        this.title = title;
        this.text = text;
        this.movie = movie;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public short getGrade() {
        return grade;
    }

    public void setGrade(short grade) {
        this.grade = grade;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MovieEntity getMovie() {
        return movie;
    }

    public void setMovie(MovieEntity movie) {
        this.movie = movie;
    }
}
