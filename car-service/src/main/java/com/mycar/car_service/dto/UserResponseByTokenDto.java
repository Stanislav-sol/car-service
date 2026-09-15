package com.mycar.car_service.dto;

import java.util.List;

public record UserResponseByTokenDto(
        String username,
        List<String> roles
) {}