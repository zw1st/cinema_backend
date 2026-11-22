package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.entity.enumeration.UserStatusRequestCondition;

@Entity
@Table(name = "special_status_user")
public class SpecialStatusUserEntity extends BaseEntity {

    @JoinColumn(name = "special_status_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SpecialStatusEntity specialStatus;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "granted_at")
    private LocalDate grantedAt;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatusRequestCondition status;

    public SpecialStatusUserEntity() {
        super();
    }

    public SpecialStatusEntity getSpecialStatus() {
        return specialStatus;
    }

    public void setSpecialStatus(SpecialStatusEntity specialStatus) {
        this.specialStatus = specialStatus;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDate getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDate grantedAt) {
        this.grantedAt = grantedAt;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public UserStatusRequestCondition getStatus() {
        return status;
    }

    public void setStatus(UserStatusRequestCondition status) {
        this.status = status;
    }

    public SpecialStatusUserEntity(SpecialStatusEntity specialStatus, UserEntity user, LocalDateTime requestedAt,
            LocalDate grantedAt, LocalDate expireDate, UserStatusRequestCondition status) {
        this.specialStatus = specialStatus;
        this.user = user;
        this.requestedAt = requestedAt;
        this.grantedAt = grantedAt;
        this.expireDate = expireDate;
        this.status = status;
    }
}