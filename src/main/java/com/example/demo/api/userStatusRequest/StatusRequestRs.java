package com.example.demo.api.userStatusRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.api.userStatus.UserStatusRs;
import com.example.demo.entity.SpecialStatusUserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record StatusRequestRs(
        Long id,
        @JsonProperty("requested_at") LocalDateTime requestedAt,
        UserStatusRs specialStatus,
        String status,
        @JsonProperty("granted_at") LocalDate grantedAt,
        @JsonProperty("expire_date") LocalDate expireDate) {
    public static StatusRequestRs from(SpecialStatusUserEntity entity) {
        return new StatusRequestRs(
                entity.getId(),
                entity.getRequestedAt(),
                UserStatusRs.from(entity.getSpecialStatus()),
                entity.getStatus().name(),
                entity.getGrantedAt(),
                entity.getExpireDate());
    }
}