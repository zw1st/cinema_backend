package com.example.demo.api.movie;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.configuration.Constants;
import com.example.demo.service.MovieService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

@RestController
@RequestMapping(Constants.API_URL + MovieController.URL)
public class MovieController {
    public static final String URL = "/movie";
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<MovieRs> getAll() {
        return movieService.getAll();
    }

    @GetMapping("/{id}")
    public MovieRs get(
            @Parameter(description = "Unique movie identifier", required = true, example = "1") @PathVariable Long id) {
        return movieService.get(id);
    }

    @PostMapping
    public MovieRs create(@RequestBody @Valid MovieRq dto) {
        return movieService.create(dto);
    }

    // @PutMapping("/{id}")
    // public MovieRs update(@PathVariable Long id, @RequestBody @Valid MovieRq dto)
    // {
    // return movieService.update(id, dto);
    // }

    @DeleteMapping("/{id}")
    public MovieRs delete(@PathVariable Long id) {
        return movieService.delete(id);
    }
}
