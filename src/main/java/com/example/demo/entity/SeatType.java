package com.example.demo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "seat_type")
public class SeatType extends BaseEntity {

    @Column(nullable = false)
    String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal coef;

    public SeatType(String name, BigDecimal coef) {
        this.name = name;
        this.coef = coef;
    }

    public SeatType() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCoef() {
        return coef;
    }

    public void setCoef(BigDecimal coef) {
        this.coef = coef;
    }

}
