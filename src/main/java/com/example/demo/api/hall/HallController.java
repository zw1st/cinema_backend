package com.example.demo.api.hall;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.HallService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/halls")
public class HallController {
    private final HallService service;

    public HallController(HallService service) {
        this.service = service;
    }

    @GetMapping
    public List<HallRs> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public HallRs get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public HallRs create(@Valid @RequestBody HallRq dto) {
        return service.create(dto);
    }
}