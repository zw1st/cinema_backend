package com.example.demo.api.userGiftCard;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.UserGiftCardService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/1.0/users/me/gift-cards")
public class UserGiftCardController {

    private final UserGiftCardService userGiftCardService;

    public UserGiftCardController(UserGiftCardService userGiftCardService) {
        this.userGiftCardService = userGiftCardService;
    }

    @PostMapping("/purchase")
    public UserGiftCardRs purchase(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserGiftCardRq rq) {
        return userGiftCardService.purchase(userId, rq);
    }

    @GetMapping
    public List<UserGiftCardRs> getMyCards(@AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "false") Boolean purchasedOnly) {
        return userGiftCardService.getCardsByOwner(userId, purchasedOnly);
    }

    // 🔹 Явная активация карт, привязанных к email (можно вызывать после логина)
    @PostMapping("/activate") // TODO привязать карту к пользователю при создании аккаунта
    public void activateCards(@AuthenticationPrincipal Long userId) {
        userGiftCardService.activateForUser(userId);
    }
}