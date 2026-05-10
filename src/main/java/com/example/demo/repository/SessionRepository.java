package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SessionEntity;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    List<SeatEntity> findByHallId(Long hallId);

    List<SessionEntity> findByMovieIdAndDate(Long movieId, LocalDate date);

}
