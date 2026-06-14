package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.MovieEntity;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {
    List<MovieEntity> findByTitleContainingIgnoreCase(String title);

    @Query("""
                select distinct m
                from MovieEntity m
                join SessionEntity s on s.movie.id = m.id
                where
                    s.date > CURRENT_DATE
                    or (
                        s.date = CURRENT_DATE
                        and s.startTime > CURRENT_TIME
                    )
                order by m.title
            """)
    List<MovieEntity> findAllVisible();

    @Query("""
                select distinct m
                from MovieEntity m
                join SessionEntity s on s.movie.id = m.id
                where m.isActive = true
                and lower(m.title) like lower(concat('%', :title, '%'))
                and (
                    s.date > CURRENT_DATE
                    or (
                        s.date = CURRENT_DATE
                        and s.startTime > CURRENT_TIME
                    )
                )
                order by m.title
            """)
    List<MovieEntity> searchVisible(@Param("title") String title);
}