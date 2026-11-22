package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.SpecialStatusEntity;

import java.util.List;

public interface SpecialStatusRepository extends JpaRepository<SpecialStatusEntity, Long> {

    List<SpecialStatusEntity> findByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}