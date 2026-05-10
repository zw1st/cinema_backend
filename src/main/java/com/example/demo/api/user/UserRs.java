package com.example.demo.api.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.entity.UserEntity;

public record UserRs(
        Long id,
        String email,
        String name,
        String avatarUrl,
        LocalDateTime createdAt) {

    public static UserRs from(UserEntity entity) {
        return new UserRs(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getAvatarUrl(),
                entity.getCreatedAt());
    }

    public static List<UserRs> fromList(Iterable<UserEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(UserRs::from)
                .toList();
    }
}