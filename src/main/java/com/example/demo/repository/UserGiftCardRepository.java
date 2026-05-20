package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.UserGiftCardEntity;
import com.example.demo.entity.enumeration.GiftCardStatus;
import com.example.demo.entity.enumeration.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface UserGiftCardRepository extends JpaRepository<UserGiftCardEntity, Long> {

        // 🔹 Пользователь: список всех своих карт
        List<UserGiftCardEntity> findByOwnerId(Long ownerId);

        // 🔹 Активация: поиск карт, подаренных на email (до регистрации/входа)
        List<UserGiftCardEntity> findByRecipientEmailAndStatus(String email, GiftCardStatus status);

        // 🔹 Оплата: поиск доступных карт (статус purchased и срок ещё не истёк)
        List<UserGiftCardEntity> findByOwnerIdAndStatusAndExpireDateAfter(
                        Long ownerId, GiftCardStatus status, LocalDate date);

        // 🔹 Шедулер: карты, которые нужно перевести в expired
        List<UserGiftCardEntity> findByStatusAndExpireDateBefore(GiftCardStatus status, LocalDate date);

        // 🔹 Фильтрация для админки/отчетов
        List<UserGiftCardEntity> findByStatus(GiftCardStatus status);

}