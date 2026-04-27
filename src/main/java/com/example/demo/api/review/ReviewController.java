package com.example.demo.api.review;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.configuration.Constants;
import com.example.demo.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.API_URL + ReviewController.URL)
public class ReviewController {
    public static final String URL = "/review";
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public List<ReviewRs> get(@PathVariable Long id) {
        return reviewService.getAllByMovieId(id);
    }

    @PostMapping
    public ReviewRs create(@RequestBody @Valid ReviewRq dto) {
        return reviewService.create(dto);
    }
}
