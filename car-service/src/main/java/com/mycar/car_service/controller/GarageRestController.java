package com.mycar.car_service.controller;

import com.mycar.car_service.model.GarageStats;
import com.mycar.car_service.service.GarageManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/garage")
public class GarageRestController {
    private static final Logger log = LoggerFactory.getLogger(GarageRestController.class);
    private final GarageManagementService garageManagementService;

    public GarageRestController(GarageManagementService garageManagementService){
        this.garageManagementService = garageManagementService;
    }

    @GetMapping("/stats")
    public ResponseEntity<GarageStats> getGarageStats() {
        log.info("GET метод, Админ запросил статус гаража");
        return ResponseEntity.ok(garageManagementService.getGarageStats());
    }

}
