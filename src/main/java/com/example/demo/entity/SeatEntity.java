package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "seat")
public class SeatEntity extends BaseEntity {

    @Column
    @Min(0)
    private int rowNum;

    @Column
    @Min(0)
    private int colNum;

    @JoinColumn(name = "seat_type_id", nullable = false)
    @ManyToOne
    private SeatType seatType;

    @JoinColumn(name = "hall_id", nullable = false)
    @ManyToOne
    private HallEntity hall;

    public SeatEntity() {
        super();
    }

    public SeatEntity(@Min(0) int rowNum, @Min(0) int colNum, SeatType seatType, HallEntity hall) {
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.seatType = seatType;
        this.hall = hall;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public HallEntity getHall() {
        return hall;
    }

    public void setHall(HallEntity hall) {
        this.hall = hall;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
    }
}
