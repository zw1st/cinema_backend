package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.SpecialStatusUserEntity;
import com.example.demo.entity.enumeration.UserStatusRequestCondition;

import java.util.List;
import java.util.Optional;

public interface SpecialStatusUserRepository extends JpaRepository<SpecialStatusUserEntity, Long> {

    List<SpecialStatusUserEntity> findByUserId(Long userId);

    List<SpecialStatusUserEntity> findByUserIdAndStatus(Long userId, UserStatusRequestCondition status);

    List<SpecialStatusUserEntity> findByStatus(UserStatusRequestCondition status);

    Optional<SpecialStatusUserEntity> findByIdAndUserId(Long id, Long userId);

}