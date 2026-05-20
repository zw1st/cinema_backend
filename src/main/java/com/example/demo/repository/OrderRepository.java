package com.example.demo.repository;

import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.enumeration.OrderStatus;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // 🔹 Активные заказы пользователя (для отображения в профиле)
    List<OrderEntity> findByUserIdAndStatus(Long userId, OrderStatus status);

    // 🔹 Безопасное получение заказа: проверяем, что он принадлежит пользователю
    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

    // 🔹 Все заказы пользователя (для полной истории)
    List<OrderEntity> findByUserId(Long userId);

    // 🔹 Поиск по статусу (для админ-панели или фоновых задач)
    List<OrderEntity> findByStatus(OrderStatus status);

    @Transactional
    void deleteAll();

    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.createdAt < :threshold")
    List<OrderEntity> findExpiredPendingOrders(
            @Param("status") OrderStatus status,
            @Param("threshold") Instant threshold);

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.tickets t " +
            "LEFT JOIN FETCH t.session " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<OrderEntity> findByUserIdWithTickets(@Param("userId") Long userId);

    boolean existsByAppliedGiftCardId(Long cardId);
}