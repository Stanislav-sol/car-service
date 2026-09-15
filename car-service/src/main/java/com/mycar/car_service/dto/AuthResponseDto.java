package com.mycar.car_service.dto;

public record AuthResponseDto(
        String token,
        String tokenType
) {
    public AuthResponseDto(String token) {
        this(token, "Bearer");
    }
}