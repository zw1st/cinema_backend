package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.userStatus.UserDiscountRs;
import com.example.demo.api.userStatus.UserStatusRq;
import com.example.demo.api.userStatus.UserStatusRs;
import com.example.demo.entity.SpecialStatusEntity;
import com.example.demo.entity.SpecialStatusUserEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.entity.enumeration.UserStatusRequestCondition;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.SpecialStatusRepository;
import com.example.demo.repository.SpecialStatusUserRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class SpecialStatusService {

    private final SpecialStatusRepository statusRepository;
    private final SpecialStatusUserRepository requestRepository;

    public SpecialStatusService(SpecialStatusRepository statusRepository,
            SpecialStatusUserRepository requestRepository) {
        this.statusRepository = statusRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public UserStatusRs create(UserStatusRq rq) {
        SpecialStatusEntity entity = new SpecialStatusEntity();
        entity.setName(rq.name());
        entity.setActive(rq.isActive());
        entity.setDiscountSize(rq.discountSize());
        return UserStatusRs.from(statusRepository.save(entity));
    }

    public UserStatusRs getById(Long id) {
        SpecialStatusEntity entity = statusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Status not found"));
        return UserStatusRs.from(entity);
    }

    public List<UserStatusRs> getAll() {
        return statusRepository.findAll().stream()
                .map(UserStatusRs::from)
                .toList();
    }

    @Transactional
    public UserStatusRs update(Long id, UserStatusRq rq) {
        SpecialStatusEntity entity = statusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Status not found"));
        entity.setName(rq.name());
        entity.setActive(rq.isActive());
        entity.setDiscountSize(rq.discountSize());
        return UserStatusRs.from(statusRepository.save(entity));
    }

    public UserDiscountRs getBestDiscount(Long userId) {
        // 🔹 1. Получаем активные статусы (валидация сроков уже выполнена шедулером)
        List<SpecialStatusUserEntity> activeRequests = requestRepository.findByUserIdAndStatus(userId,
                UserStatusRequestCondition.active);

        if (activeRequests.isEmpty()) {
            return new UserDiscountRs(false, BigDecimal.ONE);
        }

        // 🔹 2. Ищем наименьший коэффициент (наибольшая скидка)
        var bestRequest = activeRequests.stream()
                .min(Comparator.comparing(req -> req.getSpecialStatus().getDiscountSize()))
                .orElseThrow();

        return new UserDiscountRs(true, bestRequest.getSpecialStatus().getDiscountSize());
    }
}