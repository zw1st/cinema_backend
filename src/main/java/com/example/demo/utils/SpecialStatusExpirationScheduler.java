package com.example.demo.utils;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.SpecialStatusUserEntity;
import com.example.demo.entity.enumeration.UserStatusRequestCondition;
import com.example.demo.repository.SpecialStatusUserRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class SpecialStatusExpirationScheduler {

    private final SpecialStatusUserRepository requestRepository;

    public SpecialStatusExpirationScheduler(SpecialStatusUserRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expireOutdatedStatuses() {
        LocalDate today = LocalDate.now();
        List<SpecialStatusUserEntity> activeRequests = requestRepository
                .findByStatus(UserStatusRequestCondition.active);

        long expiredCount = 0;
        for (SpecialStatusUserEntity req : activeRequests) {
            if (req.getExpireDate() != null && !req.getExpireDate().isAfter(today)) {
                req.setStatus(UserStatusRequestCondition.expired);
                expiredCount++;
            }
        }

        if (expiredCount > 0) {
            requestRepository.saveAll(activeRequests);
            // В продакшене здесь лучше использовать @Modifying @Query для batch-update,
            // но для MVP saveAll достаточно и безопасен с точки зрения Dirty Checking.
        }
    }
}