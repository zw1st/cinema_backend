package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.GiftCardEntity;

import java.util.List;
import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCardEntity, Long> {

    // 🔹 Для отображения доступных номиналов в UI при покупке
    List<GiftCardEntity> findByIsActiveTrue();

    // 🔹 Валидация перед покупкой: проверка существования номинала
    Optional<GiftCardEntity> findByIdAndIsActiveTrue(Long id);
}