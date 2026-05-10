package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hall")
public class HallEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public HallEntity(String name) {
        this.name = name;
    }

    public HallEntity() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
