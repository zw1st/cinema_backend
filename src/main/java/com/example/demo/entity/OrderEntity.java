package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.demo.entity.enumeration.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"order\"")
public class OrderEntity extends BaseEntity {

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Сумма до скидок

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public OrderEntity() {
        super();
    }

    public OrderEntity(BigDecimal totalAmount, Instant createdAt, UserEntity user, String customerEmail,
            OrderStatus status) {
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.user = user;
        this.customerEmail = customerEmail;
        this.status = status;
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

    // @Column(name = "discount_coef", precision = 5, scale = 2)
    // private BigDecimal discountCoef; // 1.0 = без скидки, 0.9 = -10%

    // @Column(nullable = false, precision = 10, scale = 2)
    // private BigDecimal total; // Итоговая сумма к оплате (после скидки)
}