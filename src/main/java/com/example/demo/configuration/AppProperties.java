package com.example.demo.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Component
@ConfigurationProperties(prefix = "app.cinema")
@Validated
public class AppProperties {

    @NotNull
    private LocalTime openTime;

    @NotNull
    private LocalTime closeTime;

    @Min(0)
    private int cleanupBufferMinutes = 30; // значение по умолчанию

    @Min(1)
    private int ticketPerOrder = 5;

    @Min(0)
    private int noRefundBeforeSession = 60;

    private ZoneId timezone;

    public ZoneId getTimezone() {
        return timezone;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    public int getTicketPerOrder() {
        return ticketPerOrder;
    }

    public void setTicketPerOrder(int ticketPerOrder) {
        this.ticketPerOrder = ticketPerOrder;
    }

    public int getNoRefundBeforeSession() {
        return noRefundBeforeSession;
    }

    public void setNoRefundBeforeSession(int noRefundBeforeSession) {
        this.noRefundBeforeSession = noRefundBeforeSession;
    }

    // 🔹 Геттеры (обязательно для @ConfigurationProperties)
    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public int getCleanupBufferMinutes() {
        return cleanupBufferMinutes;
    }

    // 🔹 Сеттеры (обязательно для @ConfigurationProperties)
    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public void setCleanupBufferMinutes(int cleanupBufferMinutes) {
        this.cleanupBufferMinutes = cleanupBufferMinutes;
    }

    // 🔹 Удобный метод для проверки: входит ли время в рабочий диапазон
    public boolean isWithinWorkingHours(LocalTime time) {
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    @Min(1)
    private int reservationTimer = 15; // время в минутах

    public int getReservationTimer() {
        return reservationTimer;
    }

    public void setReservationTimer(int reservationTimer) {
        this.reservationTimer = reservationTimer;
    }

}