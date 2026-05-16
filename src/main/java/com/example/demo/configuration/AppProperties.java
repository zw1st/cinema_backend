package com.example.demo.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

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