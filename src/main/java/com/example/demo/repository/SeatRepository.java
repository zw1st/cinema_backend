package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.SeatEntity;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByHallId(Long hallId);

    void deleteByHallId(Long hallId);

}
