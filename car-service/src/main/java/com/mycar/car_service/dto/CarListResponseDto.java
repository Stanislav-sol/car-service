package com.mycar.car_service.dto;

import java.util.List;

public record CarListResponseDto(
        List<CarResponseDto> cars
) {}