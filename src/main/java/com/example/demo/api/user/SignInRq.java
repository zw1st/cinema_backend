package com.example.demo.api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRq(
        @NotBlank String firebaseUid,
        @NotBlank @Email String email,
        @Size(max = 50) String name) {
}