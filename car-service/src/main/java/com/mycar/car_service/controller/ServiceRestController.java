package com.mycar.car_service.controller;

import com.mycar.car_service.dto.ServiceRequestDto;
import com.mycar.car_service.dto.ServiceResponseDto;
import com.mycar.car_service.model.ServiceStatus;
import com.mycar.car_service.service.ServiceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceRestController {
    private static final Logger log = LoggerFactory.getLogger(ServiceRestController.class);
    private final ServiceService serviceService;

    public ServiceRestController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDto>> getAllServices(@RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(required = false) String carBrand) {
        log.info("GET запрос списка услуг с фильтрацией");
        List<ServiceResponseDto> services = serviceService.getAllServices(minPrice, maxPrice, status, carBrand);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> getServiceById(@PathVariable Long id) {
        log.info("GET запрос услуги с id: {}", id);
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ServiceResponseDto> createService(@Valid @RequestBody ServiceRequestDto request) {
        log.info("POST запрос создания услуги");
        ServiceResponseDto createdService = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ServiceResponseDto> updateService(@PathVariable Long id,
                                                            @Valid @RequestBody ServiceRequestDto request) {
        log.info("PUT запрос обновления услуги с id: {}", id);
        ServiceResponseDto updatedService = serviceService.updateService(id, request);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        log.info("DELETE запрос удаления услуги с id: {}", id);
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}