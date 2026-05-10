package com.example.demo.api.hall;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HallRq(
        @NotBlank @Size(max = 50) String name) {
}
