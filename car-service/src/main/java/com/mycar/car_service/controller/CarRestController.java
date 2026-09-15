package com.mycar.car_service.controller;

import com.mycar.car_service.dto.CarResponseDto;
import com.mycar.car_service.dto.ServiceResponseDto;
import com.mycar.car_service.dto.ServiceRequestDto;
import com.mycar.car_service.exception.CarNotFoundException;
import com.mycar.car_service.model.Car;
import com.mycar.car_service.model.Service;
import com.mycar.car_service.service.CarService;
import com.mycar.car_service.service.GarageManagementService;
import com.mycar.car_service.service.ServiceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
public class CarRestController {
    private static final Logger log = LoggerFactory.getLogger(CarRestController.class);
    private final CarService carService;
    private final ServiceService serviceService;
    private final GarageManagementService garageManagementService;

    public CarRestController(CarService carService, ServiceService serviceService,
                             GarageManagementService garageManagementService) {
        this.carService = carService;
        this.serviceService = serviceService;
        this.garageManagementService = garageManagementService;
    }

    @GetMapping
    public ResponseEntity<List<CarResponseDto>> getAllCars(@RequestParam(required = false) String brand,
                                                           @RequestParam(required = false) String model) {
        boolean hasNoFilter = (brand == null || brand.isBlank()) && (model == null || model.isBlank());

        List<CarResponseDto> cars = hasNoFilter
                ? carService.getAllCars().cars()
                : carService.searchCars(brand, model);

        if (cars.isEmpty()) {
            throw new CarNotFoundException("Машины по заданным параметрам не найдены");
        }
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponseDto> getCarById(@PathVariable Long id) {
        CarResponseDto car = carService.getCarById(id)
                .orElseThrow(() -> new CarNotFoundException("Машина с id " + id + " не найдена"));
        return ResponseEntity.ok(car);
    }

    @GetMapping("/oldest")
    public ResponseEntity<List<CarResponseDto>> getOldestCars() {
        List<CarResponseDto> oldCars = carService.getEarliestCars();
        if (oldCars.isEmpty()) {
            throw new CarNotFoundException("В гараже нет машин");
        }
        return ResponseEntity.ok(oldCars);
    }

    @PostMapping
    public ResponseEntity<CarResponseDto> createCar(@Valid @RequestBody Car car) {
        CarResponseDto createdCar = carService.addCar(car);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCar.id())
                .toUri();

        return ResponseEntity.created(location)
                .header("X-Garage-Message", "Car added to Garage")
                .body(createdCar);
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<CarResponseDto> createDuplicate(@PathVariable Long id) {
        Car original = carService.getCarEntityById(id)
                .orElseThrow(() -> new CarNotFoundException("Машина с id " + id + " не найдена"));

        Car duplicate = new Car(
                original.getBrand(),
                original.getModel(),
                original.getDesignStyle(),
                original.getYear()
        );
        duplicate.setUser(original.getUser());

        CarResponseDto createdCar = carService.addCar(duplicate);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/cars/{id}")
                .buildAndExpand(createdCar.id())
                .toUri();

        return ResponseEntity.created(location).body(createdCar);
    }

    @PostMapping("/{carId}/services")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ServiceResponseDto> createServiceForCar(@PathVariable Long carId,
                                                                  @Valid @RequestBody ServiceRequestDto request) {
        ServiceResponseDto createdService = serviceService.createServiceForCar(carId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdService.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdService);
    }

    @GetMapping("/{carId}/services")
    public ResponseEntity<List<ServiceResponseDto>> getServicesByCarId(@PathVariable Long carId) {
        List<ServiceResponseDto> services = serviceService.getServicesByCarId(carId);
        return ResponseEntity.ok(services);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<CarResponseDto>> createCars(@RequestBody List<@Valid Car> cars) {
        if (cars.isEmpty()) {
            throw new IllegalArgumentException("Список машин не может быть пустым");
        }

        List<CarResponseDto> savedCars = new ArrayList<>();
        for (Car car : cars) {
            savedCars.add(carService.addCar(car));
        }

        return ResponseEntity.status(201).body(savedCars);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarResponseDto> updateCar(@PathVariable Long id, @Valid @RequestBody Car car) {
        return ResponseEntity.ok(carService.updateCar(id, car));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        boolean deleted = carService.deleteCarById(id);
        if (!deleted) {
            throw new CarNotFoundException("Машина с id " + id + " не найдена");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCarsByBrand(@RequestParam String brand) {
        boolean deleted = carService.removeAllByBrand(brand);
        if (!deleted) {
            throw new CarNotFoundException("Машины бренда " + brand + " не найдены");
        }
        return ResponseEntity.noContent().build();
    }
}