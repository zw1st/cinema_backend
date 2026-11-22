package com.example.demo.entity.enumeration;

public enum OrderStatus {
    PENDING, // Создан, ожидает оплаты
    PAID, // Оплата подтверждена
    CANCELLED // Отменён (пользователем или по таймауту)
}
