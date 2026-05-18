package com.example.demo.api.ticket;

import com.example.demo.entity.TicketEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TicketRs(
        Long id,
        Integer row,
        Integer col,
        String status,
        BigDecimal finalPrice,
        @JsonProperty("ticket_code") String ticketCode, // Nullable
        @JsonProperty("session_id") Long sessionId,
        @JsonProperty("session_date") LocalDate sessionDate,
        @JsonProperty("session_time") LocalTime sessionStartTime) {
    public static TicketRs from(TicketEntity t) {
        return new TicketRs(
                t.getId(),
                t.getRowNum(),
                t.getColNum(),
                t.getStatus().name(),
                t.getFinalPrice(),
                t.getTicketCode(),
                t.getSession().getId(),
                t.getSession().getDate(),
                t.getSession().getStartTime());
    }
}