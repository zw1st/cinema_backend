package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.demo.entity.enumeration.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"order\"")
public class OrderEntity extends BaseEntity {

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Сумма до скидок

    @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice; // Сумма после всех скидок (к оплате)

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "exchange_from_order_id")
    private Long exchangeFromOrderId; // Nullable, ссылка на старый заказ

    @Column(name = "adjustment_amount", precision = 10, scale = 2)
    private BigDecimal adjustmentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<TicketEntity> tickets; // никаких сеттеров!

    @Column(name = "status_discount_coef", precision = 3, scale = 2)
    private BigDecimal statusDiscountCoef = BigDecimal.ONE;

    @Column(name = "applied_gift_card_id")
    private Long appliedGiftCardId;

    public OrderEntity(BigDecimal totalAmount, BigDecimal finalPrice, Instant createdAt, UserEntity user,
            String customerEmail, Long exchangeFromOrderId, BigDecimal adjustmentAmount, OrderStatus status,
            BigDecimal statusDiscountCoef, Long appliedGiftCardId) {
        this.totalAmount = totalAmount;
        this.finalPrice = finalPrice;
        this.createdAt = createdAt;
        this.user = user;
        this.customerEmail = customerEmail;
        this.exchangeFromOrderId = exchangeFromOrderId;
        this.adjustmentAmount = adjustmentAmount;
        this.status = status;
        this.statusDiscountCoef = statusDiscountCoef;
        this.appliedGiftCardId = appliedGiftCardId;
    }

    public BigDecimal getStatusDiscountCoef() {
        return statusDiscountCoef;
    }

    public void setStatusDiscountCoef(BigDecimal statusDiscountCoef) {
        this.statusDiscountCoef = statusDiscountCoef;
    }

    public Long getAppliedGiftCardId() {
        return appliedGiftCardId;
    }

    public void setAppliedGiftCardId(Long appliedGiftCardId) {
        this.appliedGiftCardId = appliedGiftCardId;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public OrderEntity() {
        super();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getAdjustmentAmount() {
        return adjustmentAmount;
    }

    public void setAdjustmentAmount(BigDecimal adjustmentAmount) {
        this.adjustmentAmount = adjustmentAmount;
    }

    public Long getExchangeFromOrderId() {
        return exchangeFromOrderId;
    }

    public void setExchangeFromOrderId(Long exchangeFromOrderId) {
        this.exchangeFromOrderId = exchangeFromOrderId;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
}