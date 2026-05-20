package com.example.demo.repository;

import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.enumeration.TicketStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    Optional<TicketEntity> findByTicketCode(String ticketCode);

    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @Query("SELECT t FROM TicketEntity t WHERE t.ticketCode = :code")
    // Optional<TicketEntity> findByTicketCodeForUpdate(@Param("code") String
    // ticketCode);

    List<TicketEntity> findBySessionIdAndStatus(Long sessionId, TicketStatus status);

    List<TicketEntity> findByOrderId(Long orderId);

    boolean existsBySessionIdAndRowNumAndColNumAndStatusIn(
            Long sessionId, Integer row, Integer col, List<TicketStatus> statuses);

    List<TicketEntity> findByOrderIdAndStatus(Long orderId, TicketStatus status);

    @Transactional
    void deleteAll();

    @Query("SELECT t FROM TicketEntity t WHERE t.order.id IN :orderIds AND t.status = :status")
    List<TicketEntity> findByOrderIdsAndStatus(
            @Param("orderIds") List<Long> orderIds,
            @Param("status") TicketStatus status);

    Optional<TicketEntity> findBySessionIdAndRowNumAndColNumAndStatus(
            Long sessionId, Integer row, Integer col, TicketStatus status);

    List<TicketEntity> findByOrderIdAndIdIn(Long orderId, List<Long> ids);

    List<TicketEntity> findByIdIn(List<Long> ids);

    List<TicketEntity> findBySessionId(Long sessionId);

    @Query("SELECT t FROM TicketEntity t WHERE t.order.id IN :orderIds AND t.status IN :statuses")
    List<TicketEntity> findByOrderIdsAndStatusIn(
            @Param("orderIds") List<Long> orderIds,
            @Param("statuses") List<TicketStatus> statuses);
}