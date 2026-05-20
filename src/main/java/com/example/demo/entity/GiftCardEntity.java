package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "gift_card")
public class GiftCardEntity extends BaseEntity {

    @Column(nullable = false, unique = true, precision = 10, scale = 2)
    private BigDecimal nominal;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public GiftCardEntity(BigDecimal nominal, boolean isActive) {
        this.nominal = nominal;
        this.isActive = isActive;
    }

    public GiftCardEntity() {
        super();
    }

    public BigDecimal getNominal() {
        return nominal;
    }

    public void setNominal(BigDecimal nominal) {
        this.nominal = nominal;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}