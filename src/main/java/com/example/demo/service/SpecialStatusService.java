package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.userStatus.UserStatusRq;
import com.example.demo.api.userStatus.UserStatusRs;
import com.example.demo.entity.SpecialStatusEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.SpecialStatusRepository;

import java.util.List;

@Service
public class SpecialStatusService {

    private final SpecialStatusRepository statusRepository;

    public SpecialStatusService(SpecialStatusRepository statusRepository) {
        this.statusRepository = statusRepository;
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
}