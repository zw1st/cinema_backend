package com.example.demo.entity.enumeration;

public enum GiftCardStatus {
    purchased, // Куплена, ожидает активации или использования
    activated, // Использована (списана при оплате заказа)
    expired // Истёк срок действия
}