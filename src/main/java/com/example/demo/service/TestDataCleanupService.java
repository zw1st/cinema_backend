package com.example.demo.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;

@Service
@Profile("dev") // 🔹 Сервис существует только в dev-режиме
public class TestDataCleanupService {

    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;

    public TestDataCleanupService(TicketRepository ticketRepository, OrderRepository orderRepository) {
        this.ticketRepository = ticketRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void cleanupPurchaseData() {
        ticketRepository.deleteAll();
        orderRepository.deleteAll();
    }
}