package com.example.demo.api.ticket;

import com.example.demo.api.session.SessionShortRs;
import com.example.demo.entity.TicketEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record TicketRs(
        Long id,
        Integer row,
        Integer col,
        String status,
        BigDecimal finalPrice,
        @JsonProperty("ticket_code") String ticketCode, // Nullable
        SessionShortRs session) {
    public static TicketRs from(TicketEntity t) {
        return new TicketRs(
                t.getId(),
                t.getRowNum(),
                t.getColNum(),
                t.getStatus().name(),
                t.getFinalPrice(),
                t.getTicketCode(),
                SessionShortRs.from(t.getSession()));
    }
}