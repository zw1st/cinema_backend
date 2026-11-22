package com.example.demo.utils;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.UserGiftCardEntity;
import com.example.demo.entity.enumeration.GiftCardStatus;
import com.example.demo.repository.UserGiftCardRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class GiftCardExpirationScheduler {

    private final UserGiftCardRepository userGiftCardRepository;

    public GiftCardExpirationScheduler(UserGiftCardRepository userGiftCardRepository) {
        this.userGiftCardRepository = userGiftCardRepository;
    }

    // 🔹 Запускается ежедневно в 03:00
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expireOutdated() {
        LocalDate today = LocalDate.now();
        List<UserGiftCardEntity> outdated = userGiftCardRepository
                .findByStatusAndExpireDateBefore(GiftCardStatus.purchased, today);

        if (outdated.isEmpty())
            return;

        outdated.forEach(c -> c.setStatus(GiftCardStatus.expired));
        userGiftCardRepository.saveAll(outdated);
    }
}
