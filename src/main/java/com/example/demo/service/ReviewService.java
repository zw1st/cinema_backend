package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.review.ReviewRq;
import com.example.demo.api.review.ReviewRs;
import com.example.demo.entity.MovieEntity;
import com.example.demo.entity.ReviewEntity;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ReviewRepository;

@Service
public class ReviewService {
    private ReviewRepository reviewRepository;
    private MovieRepository movieRepo;

    public ReviewService(ReviewRepository reviewRepository, MovieRepository movieRepo) {
        this.reviewRepository = reviewRepository;
        this.movieRepo = movieRepo;
    }

    @Transactional(readOnly = true)
    public List<ReviewRs> getAllByMovieId(Long movieId) {
        return ReviewRs.fromList(reviewRepository.findByMovieId(movieId));
    }

    @Transactional
    public ReviewRs create(ReviewRq dto) {
        final MovieEntity movie = movieRepo.getReferenceById(dto.movieId());
        ReviewEntity entity = new ReviewEntity(dto.author(), dto.grage(), dto.date(), dto.title(), dto.text(), movie);
        entity = reviewRepository.save(entity);
        return ReviewRs.from(entity);
    }

}
