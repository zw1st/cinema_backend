package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hall")
public class HallEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @Column(name = "total_cols", nullable = false)
    private Integer totalCols;

    public HallEntity(String name, Integer totalRows, Integer totalCols) {
        this.name = name;
        this.totalRows = totalRows;
        this.totalCols = totalCols;
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

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getTotalCols() {
        return totalCols;
    }

    public void setTotalCols(Integer totalCols) {
        this.totalCols = totalCols;
    }
}
