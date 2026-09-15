package com.mycar.car_service.model;

import java.util.List;

public record GarageStats(
        int totalCars,
        int newestYear,
        int oldestYear,
        int availableSpots,
        List<String> uniqueBrands
) {}
