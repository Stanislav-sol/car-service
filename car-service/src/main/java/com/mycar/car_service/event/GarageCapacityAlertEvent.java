package com.mycar.car_service.event;

public record GarageCapacityAlertEvent(
        int maxCapacity,
        int currentCapacity
) {}
