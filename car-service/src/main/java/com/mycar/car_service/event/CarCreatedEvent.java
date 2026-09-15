package com.mycar.car_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public record CarCreatedEvent(
        Long carId,
        String brand,
        String model,
        int year,
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}