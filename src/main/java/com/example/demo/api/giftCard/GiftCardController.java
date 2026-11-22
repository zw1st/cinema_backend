package com.example.demo.api.giftCard;

import org.springframework.web.bind.annotation.*;

import com.example.demo.service.GiftCardService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/1.0/admin/gift-card-types")
public class GiftCardController {

    private final GiftCardService giftCardService;

    public GiftCardController(GiftCardService giftCardService) {
        this.giftCardService = giftCardService;
    }

    @PostMapping
    public GiftCardRs createType(@Valid @RequestBody GiftCardRq rq) {
        return giftCardService.create(rq);
    }

    @GetMapping
    public List<GiftCardRs> getAllTypes() {
        return giftCardService.getAllActive();
    }
}