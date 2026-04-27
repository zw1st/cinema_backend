package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.demo.api.movie.MovieRq;
import com.example.demo.api.movie.MovieRs;
import com.example.demo.entity.MovieEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.MovieRepository;

@Service
public class MovieService {
    private final MovieRepository repository;
    private final TransactionTemplate transactionTemplate;

    public MovieService(MovieRepository movieRepository, TransactionTemplate transactionTemplate) {
        this.repository = movieRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MovieEntity getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(MovieEntity.class, id));
    }

    @Transactional(readOnly = true)
    public List<MovieRs> getAll() {
        return MovieRs.fromList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public MovieRs get(Long id) {
        final MovieEntity entity = getEntity(id);
        return MovieRs.from(entity);
    }

    // @Transactional
    // public MovieRs create(MovieRq dto) {
    // MovieEntity entity = new MovieEntity(
    // dto.title(),
    // dto.releaseDate(),
    // dto.isActive(),
    // dto.rating(),
    // dto.duration(),
    // dto.ageRating(),
    // dto.description(),
    // dto.posterImageUrl(),
    // dto.genres(),
    // dto.actors(),
    // dto.directors());
    // entity = repository.save(entity);
    // return MovieRs.from(entity);
    // }

    public MovieRs create(MovieRq dto) {
        return transactionTemplate.execute(status -> {
            // Вся логика внутри этой лямбды выполняется в одной транзакции
            MovieEntity entity = new MovieEntity(
                    dto.title(),
                    dto.releaseDate(),
                    dto.isActive(),
                    dto.rating(),
                    dto.duration(),
                    dto.ageRating(),
                    dto.description(),
                    dto.posterImageUrl(),
                    dto.genres(),
                    dto.actors(),
                    dto.directors());

            entity = repository.save(entity); // INSERT + flush при необходимости
            return MovieRs.from(entity); // результат вернётся из метода
        });
    }

    // @Transactional
    // public MovieRs update(Long id, MovieRs dto) {
    // StudentEntity entity = getEntity(id);
    // entity.setLastName(dto.lastName());
    // entity.setFirstName(dto.firstName());
    // entity.setEmail(dto.email());
    // entity.setPhone(dto.phone());
    // entity.setBdate(LocalDate.parse(dto.bdate()));
    // entity.setGroup(typeService.getEntity(dto.groupId()));
    // entity.setImage(dto.image());
    // entity = repository.save(entity);
    // return StudentRs.from(entity);
    // }

    @Transactional
    public MovieRs delete(Long id) {
        final MovieEntity entity = getEntity(id);
        repository.delete(entity);
        return MovieRs.from(entity);
    }
}
