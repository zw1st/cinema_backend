package com.example.demo.api.userStatusRequest;

import org.springframework.web.bind.annotation.*;

import com.example.demo.service.SpecialStatusRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/1.0/admin/status-requests")
public class AdminStatusRequestController {

    private final SpecialStatusRequestService requestService;

    public AdminStatusRequestController(SpecialStatusRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/pending")
    public List<StatusRequestRs> getPendingRequests() {
        return requestService.getPendingRequests();
    }

    @PatchMapping("/{id}/approve")
    public StatusRequestRs approveRequest(@PathVariable Long id) {
        return requestService.approveRequest(id);
    }

    @PatchMapping("/{id}/decline")
    public StatusRequestRs declineRequest(@PathVariable Long id) {
        return requestService.declineRequest(id);
    }
}