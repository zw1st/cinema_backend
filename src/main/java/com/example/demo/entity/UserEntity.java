package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 128, name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 50, nullable = true, unique = false)
    private String name;

    @Column(name = "avatar_url", length = 255, nullable = true, unique = false)
    private String avatarUrl;

    public UserEntity() {
        super();
    }

    public UserEntity(String email, String firebaseUid, LocalDateTime createdAt, String name, String avatarUrl) {
        this.email = email;
        this.firebaseUid = firebaseUid;
        this.createdAt = createdAt;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
