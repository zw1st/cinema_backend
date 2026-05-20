package com.example.demo.api.session;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.SessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/sessions")
public class SessionController {
    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @GetMapping
    public List<SessionRs> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public SessionRs get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public SessionRs create(@Valid @RequestBody SessionRq dto) {
        return service.create(dto);
    }

    @GetMapping("/search")
    public List<SessionRs> getByMovieAndDate(
            @RequestParam("movie_id") Long movieId,
            @RequestParam("date") LocalDate date) {
        return service.getByMovieIdAndDate(movieId, date);
    }

    @GetMapping("/movie/{movie_id}")
    public List<SessionRs> getByMovieAndDate(
            @PathVariable("movie_id") Long movieId) {
        return service.getByMovieId(movieId);
    }

    @GetMapping("/layout/{session_id}")
    public LayoutRs getLayout(@PathVariable Long session_id) {
        return service.getLayout(session_id);
    }
}