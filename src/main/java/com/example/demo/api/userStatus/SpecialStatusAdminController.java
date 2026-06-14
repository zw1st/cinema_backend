package com.example.demo.api.userStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.SpecialStatusService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/1.0/admin/statuses")
public class SpecialStatusAdminController {

    private final SpecialStatusService statusService;

    public SpecialStatusAdminController(SpecialStatusService statusService) {
        this.statusService = statusService;
    }

    @PostMapping
    public UserStatusRs createStatus(@Valid @RequestBody UserStatusRq rq) {
        return statusService.create(rq);
    }

    @GetMapping
    public List<UserStatusRs> getAllStatuses() {
        return statusService.getAll();
    }

    @GetMapping("/{id}")
    public UserStatusRs getStatus(@PathVariable Long id) {
        return statusService.getById(id);
    }

    @PutMapping("/{id}")
    public UserStatusRs updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRq rq) {
        return statusService.update(id, rq);
    }

    @GetMapping("/get-best-discount/me")
    public UserDiscountRs getBestDiscount(@AuthenticationPrincipal Long user_id) {
        return statusService.getBestDiscount(user_id);
    }
}