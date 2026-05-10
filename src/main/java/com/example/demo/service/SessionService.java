package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.session.SessionRq;
import com.example.demo.api.session.SessionRs;
import com.example.demo.entity.HallEntity;
import com.example.demo.entity.MovieEntity;
import com.example.demo.entity.SessionEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.SessionRepository;

@Service
public class SessionService {
    private final SessionRepository repository;
    private final MovieService movieService;
    private final HallService hallService;

    public SessionService(
            SessionRepository repository,
            MovieService movieService,
            HallService hallService) {
        this.repository = repository;
        this.movieService = movieService;
        this.hallService = hallService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SessionEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(SessionEntity.class, id));
    }

    @Transactional(readOnly = true)
    public List<SessionRs> getAll() {
        return SessionRs.fromList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public List<SessionRs> getByMovieIdAndDate(Long movieId, LocalDate date) {
        return SessionRs.fromList(repository.findByMovieIdAndDate(movieId, date));
    }

    @Transactional(readOnly = true)
    public SessionRs get(Long id) {
        final SessionEntity entity = getEntity(id);
        return SessionRs.from(entity);
    }

    @Transactional
    public SessionRs create(SessionRq dto) {
        // Валидация связей
        MovieEntity movie = movieService.getEntity(dto.movieId());
        HallEntity hall = hallService.getEntity(dto.hallId());

        SessionEntity entity = new SessionEntity(
                movie, hall, dto.date(), dto.startTime(), dto.endTime(), dto.basePrice());
        entity = repository.save(entity);
        return SessionRs.from(entity);
    }
}