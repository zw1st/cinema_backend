package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.demo.entity.enumeration.TicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ticket")
public class TicketEntity extends BaseEntity {

    @Column(nullable = false)
    private Integer rowNum;

    @Column(nullable = false)
    private Integer colNum;

    @Column(name = "booked_at", nullable = false)
    private Instant bookedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(name = "ticket_code", unique = true, length = 255)
    private String ticketCode;

    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @JoinColumn(name = "order_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private OrderEntity order;

    @JoinColumn(name = "session_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SessionEntity session;

    public TicketEntity() {
        super();
    }

    public TicketEntity(Integer row, Integer col, Instant bookedAt, TicketStatus status, String ticketCode,
            BigDecimal finalPrice, OrderEntity order, SessionEntity session) {
        this.rowNum = row;
        this.colNum = col;
        this.bookedAt = bookedAt;
        this.status = status;
        this.ticketCode = ticketCode;
        this.finalPrice = finalPrice;
        this.order = order;
        this.session = session;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer row) {
        this.rowNum = row;
    }

    public Integer getColNum() {
        return colNum;
    }

    public void setColNum(Integer col) {
        this.colNum = col;
    }

    public Instant getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Instant bookedAt) {
        this.bookedAt = bookedAt;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public SessionEntity getSession() {
        return session;
    }

    public void setSession(SessionEntity session) {
        this.session = session;
    }
}