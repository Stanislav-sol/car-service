package com.mycar.car_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDto(
        @NotBlank(message = "Логин не должен быть пустым")
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        String login,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 4, max = 100, message = "Пароль должен быть не менее 4 символов")
        String password,

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный формат email")
        String email
) {
        public RegisterDto() {
                this("", "", "");
        }
}