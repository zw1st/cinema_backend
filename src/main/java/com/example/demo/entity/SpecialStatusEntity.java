package com.example.demo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "special_status")
public class SpecialStatusEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "discount_size", nullable = false, precision = 2, scale = 2)
    private BigDecimal discountSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public BigDecimal getDiscountSize() {
        return discountSize;
    }

    public void setDiscountSize(BigDecimal discountSize) {
        this.discountSize = discountSize;
    }

    public SpecialStatusEntity(String name, boolean isActive, BigDecimal discountSize) {
        this.name = name;
        this.isActive = isActive;
        this.discountSize = discountSize;
    }

    public SpecialStatusEntity() {
        super();
    }
}