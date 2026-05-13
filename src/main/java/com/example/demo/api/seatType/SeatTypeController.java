package com.example.demo.api.seatType;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.SeatTypeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/seat-types")
public class SeatTypeController {
    private final SeatTypeService service;

    public SeatTypeController(SeatTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<SeatTypeRs> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public SeatTypeRs get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public SeatTypeRs create(@Valid @RequestBody SeatTypeRq dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public SeatTypeRs update(@PathVariable Long id, @Valid @RequestBody SeatTypeRq dto) {
        return service.update(id, dto);
    }
}