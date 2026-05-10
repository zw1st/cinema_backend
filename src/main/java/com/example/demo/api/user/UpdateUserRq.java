package com.example.demo.api.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRq(
                @Size(max = 50) String name,
                String avatarUrl) {
}