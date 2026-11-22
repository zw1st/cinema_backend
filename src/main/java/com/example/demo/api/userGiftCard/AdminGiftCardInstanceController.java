package com.example.demo.api.userGiftCard;

import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.enumeration.GiftCardStatus;
import com.example.demo.service.UserGiftCardService;

import java.util.List;

@RestController
@RequestMapping("/api/1.0/admin/gift-cards")
public class AdminGiftCardInstanceController {

    private final UserGiftCardService userGiftCardService;

    public AdminGiftCardInstanceController(UserGiftCardService userGiftCardService) {
        this.userGiftCardService = userGiftCardService;
    }

    @GetMapping
    public List<UserGiftCardRs> getAllCards(@RequestParam(required = false) GiftCardStatus status) {
        return status == null
                ? userGiftCardService.getAllCards()
                : userGiftCardService.getCardsByStatus(status);
    }
}
