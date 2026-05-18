package com.example.demo.entity.enumeration;

public enum TicketStatus {
    RESERVED, // Временная бронь (10 минут на оплату)
    PAID, // Оплачен, готов к использованию
    SCANNED, // Использован на входе
    CANCELLED, // Отменён (таймаут или возврат)
    EXCHANGING;
}
