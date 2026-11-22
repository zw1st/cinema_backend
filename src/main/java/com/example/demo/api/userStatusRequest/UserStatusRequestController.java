package com.example.demo.api.userStatusRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.SpecialStatusRequestService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/1.0/users/me/status-requests")
public class UserStatusRequestController {

    private final SpecialStatusRequestService requestService;

    public UserStatusRequestController(SpecialStatusRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public StatusRequestRs createRequest(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateStatusRequestRq rq) {
        return requestService.createRequest(userId, rq);
    }

    @GetMapping
    public List<StatusRequestRs> getMyRequests(@AuthenticationPrincipal Long userId) {
        return requestService.getUserRequests(userId);
    }
}