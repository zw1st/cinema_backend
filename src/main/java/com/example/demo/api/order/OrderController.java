package com.example.demo.api.order;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.OrderService;
import com.example.demo.service.TicketPdfService;
import com.google.common.net.HttpHeaders;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/orders")
public class OrderController {
    private final OrderService orderService;
    private final TicketPdfService ticketPdfService;

    public OrderController(OrderService orderService, TicketPdfService ticketPdfService) {
        this.orderService = orderService;
        this.ticketPdfService = ticketPdfService;
    }

    @PostMapping
    public OrderRs createOrder(@Valid @RequestBody CreateOrderRq rq,
            @AuthenticationPrincipal Long userId) {
        return orderService.createOrder(rq, userId);
    }

    @PostMapping("/{id}/confirm")
    public OrderRs confirmPayment(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return orderService.confirmPayment(id, userId);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelWholeBooking(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        orderService.cancelBooking(orderId, null, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/tickets")
    public ResponseEntity<Void> cancelSpecificTickets(
            @PathVariable Long orderId,
            @RequestBody List<Long> ticketIds,
            @AuthenticationPrincipal Long userId) {
        orderService.cancelBooking(orderId, ticketIds, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/refund")
    public ResponseEntity<Void> refundWholeOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        orderService.refundTickets(orderId, null, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/refund/tickets")
    public ResponseEntity<Void> refundSpecificTickets(
            @PathVariable Long orderId,
            @RequestBody List<Long> ticketIds,
            @AuthenticationPrincipal Long userId) {
        orderService.refundTickets(orderId, ticketIds, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/exchange")
    public OrderRs exchangeTickets(
            @PathVariable Long orderId,
            @Valid @RequestBody ExchangeRq rq,
            @AuthenticationPrincipal Long userId) {
        return orderService.exchangeTickets(orderId, rq, userId);
    }

    @GetMapping("/me/orders")
    public List<OrderRs> getMyOrders(@AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "false") boolean withoutCancelled) {
        return orderService.getOrdersForUser(userId, withoutCancelled);
    }

    @PutMapping("/{orderId}/{ticketId}/mark-scanned")
    public ResponseEntity<Void> markTicketAsScanned(
            @PathVariable Long orderId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long userId) {
        orderService.markTicketAsScanned(orderId, ticketId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tickets/{ticketId}/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal Long userId) {

        byte[] pdf = ticketPdfService.generateTicketPdf(
                ticketId,
                userId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ticket-" + ticketId + ".pdf")
                .body(pdf);
    }
}