package com.mycar.car_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CarResponseDto(
        @NotNull Long id,
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String designStyle,
        @Min(1990) @Max(2026) int year,
        Long userId,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt
) {}