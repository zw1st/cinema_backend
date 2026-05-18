package com.example.demo.api.userStatusRequest;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record CreateStatusRequestRq(
                @JsonProperty("status_id") @NotNull Long statusId,
                @JsonProperty("expire_date") @NotNull LocalDate expireDate) {
}