package com.example.demo.api.admin;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.TestDataCleanupService;

@RestController
@RequestMapping("/api/1.0/admin")
// @Profile("dev") // 🔹 Контроллер не виден в prod
public class AdminTestDataController {

    private final TestDataCleanupService cleanupService;

    public AdminTestDataController(TestDataCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @DeleteMapping("/cleanup/purchases")
    public ResponseEntity<Void> cleanupPurchases() {
        cleanupService.cleanupPurchaseData();
        return ResponseEntity.noContent().build();
    }
}