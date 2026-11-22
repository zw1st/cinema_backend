package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.api.giftCard.GiftCardRq;
import com.example.demo.api.giftCard.GiftCardRs;
import com.example.demo.entity.GiftCardEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.GiftCardRepository;

import java.util.List;

@Service
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;

    public GiftCardService(GiftCardRepository giftCardRepository) {
        this.giftCardRepository = giftCardRepository;
    }

    public GiftCardRs create(GiftCardRq rq) {
        GiftCardEntity entity = new GiftCardEntity();
        entity.setNominal(rq.nominal());
        entity.setActive(rq.isActive());
        return GiftCardRs.from(giftCardRepository.save(entity));
    }

    public List<GiftCardRs> getAllActive() {
        return giftCardRepository.findByIsActiveTrue().stream()
                .map(GiftCardRs::from)
                .toList();
    }

    public GiftCardRs getById(Long id) {
        return giftCardRepository.findById(id)
                .map(GiftCardRs::from)
                .orElseThrow(() -> new NotFoundException("Gift card template not found"));
    }
}