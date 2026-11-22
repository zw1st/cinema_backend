package com.example.demo.api.seat;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.session.LayoutRq;
import com.example.demo.api.session.LayoutRs;
import com.example.demo.service.SeatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/seats")
public class SeatController {
    private final SeatService service;

    public SeatController(SeatService service) {
        this.service = service;
    }

    @GetMapping
    public List<SeatRs> getByHall(@RequestParam("hall_id") Long hallId) {
        return service.getAllByHallId(hallId);
    }

    @PostMapping
    public SeatRs createSingle(@Valid @RequestBody SeatRq dto) {
        return service.create(dto);
    }

    @PostMapping("/batch")
    public LayoutRs createLayout(@Valid @RequestBody LayoutRq rq) {
        return service.createLayout(rq);
    }
}