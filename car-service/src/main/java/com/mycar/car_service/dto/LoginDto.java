package com.mycar.car_service.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "Логин не должен быть пустым")
        String login,

        @NotBlank(message = "Пароль не должен быть пустым")
        String password
) {}