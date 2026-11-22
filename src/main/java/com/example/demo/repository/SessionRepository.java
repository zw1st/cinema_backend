package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SessionEntity;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    List<SeatEntity> findByHallId(Long hallId);

    List<SessionEntity> findByMovieIdAndDate(Long movieId, LocalDate date);

    List<SessionEntity> findByMovieId(Long movieId);

    List<SessionEntity> findByHallIdAndDate(Long hallId, LocalDate date);

    @Query("SELECT s FROM SessionEntity s " +
            "WHERE s.movie.id = :movieId " +
            "AND (s.date > :today OR (s.date = :today AND s.endTime >= :now))")
    List<SessionEntity> findUpcomingByMovieId(@Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now);
}
