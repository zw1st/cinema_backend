package com.example.demo.api.session;

import com.example.demo.entity.SeatEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record SeatStatusDto(
                Long id, // ID места в БД (для бронирования)
                @JsonProperty("row") Integer rowNum,
                @JsonProperty("col") Integer colNum,
                @JsonProperty("type_id") Long seatTypeId,
                @JsonProperty("type_name") String seatTypeName,
                String status, // "available", "booked", "reserved"
                @JsonProperty("base_price") BigDecimal basePrice,
                @JsonProperty("final_price") BigDecimal finalPrice // basePrice * multiplier
) {
        public static SeatStatusDto forLayout(SeatEntity seat) {
                return new SeatStatusDto(
                                seat.getId(),
                                seat.getRowNum(),
                                seat.getColNum(),
                                seat.getSeatType().getId(),
                                seat.getSeatType().getName(),
                                "available",
                                BigDecimal.ZERO,
                                BigDecimal.ZERO);
        }

        // 🔹 Для схемы сеанса (с ценой и статусами билетов)
        public static SeatStatusDto forSession(SeatEntity seat, String ticketStatus, BigDecimal sessionBasePrice) {
                BigDecimal finalPrice = sessionBasePrice.multiply(seat.getSeatType().getCoef());
                return new SeatStatusDto(
                                seat.getId(),
                                seat.getRowNum(),
                                seat.getColNum(),
                                seat.getSeatType().getId(),
                                seat.getSeatType().getName(),
                                ticketStatus != null ? ticketStatus : "available",
                                sessionBasePrice,
                                finalPrice);
        }
}