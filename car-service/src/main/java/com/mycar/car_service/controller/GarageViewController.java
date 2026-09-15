package com.mycar.car_service.controller;

import com.mycar.car_service.dto.CarResponseDto;
import com.mycar.car_service.dto.ServiceResponseDto;
import com.mycar.car_service.dto.UserResponseDto;
import com.mycar.car_service.exception.CarNotFoundException;
import com.mycar.car_service.model.Car;
import com.mycar.car_service.service.CarService;
import com.mycar.car_service.service.GarageManagementService;
import com.mycar.car_service.service.ServiceService;
import com.mycar.car_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GarageViewController {
    private static final Logger log = LoggerFactory.getLogger(GarageViewController.class);
    private final CarService carService;
    private final UserService userService;
    private final ServiceService serviceService;
    private final GarageManagementService garageManagementService;

    public GarageViewController(CarService carService, UserService userService, ServiceService serviceService,
                                GarageManagementService garageManagementService) {
        this.carService = carService;
        this.userService = userService;
        this.serviceService = serviceService;
        this.garageManagementService = garageManagementService;
    }

    @GetMapping("/dark-garage")
    public String getDarkGaragePage(@RequestParam(required = false, defaultValue = "cars") String searchType,
                                    @RequestParam(required = false) String brand,
                                    @RequestParam(required = false) Integer minYear,
                                    @RequestParam(required = false) String username,
                                    @RequestParam(required = false) String serviceName,
                                    Model model) {
        log.info("GET dark-garage type='{}', brand='{}', minYear='{}'", searchType, brand, minYear);
        populateBaseGarageModel(model);
        model.addAttribute("activeSearchType", searchType);
        List<CarResponseDto> resultCars = carService.getCars();

        if ("cars".equals(searchType)) {
            if (brand != null && !brand.isBlank()) {
                resultCars = carService.searchCars(brand, null);
                model.addAttribute("selectedBrand", brand);
            } else if (minYear != null) {
                resultCars = carService.getCarsByMinYear(minYear);
                model.addAttribute("selectedMinYear", minYear);
            }
        }
        model.addAttribute("carsList", resultCars);
        List<UserResponseDto> resultUsers = userService.getAllUsers();

        if ("users".equals(searchType) && username != null && !username.isBlank()) {
            resultUsers = resultUsers.stream()
                    .filter(u -> u.getLogin().toLowerCase().contains(username.toLowerCase()))
                    .toList();
            model.addAttribute("selectedUsername", username);
        }
        model.addAttribute("usersList", resultUsers);
        List<ServiceResponseDto> resultServices = serviceService.getAllServices();

        if ("services".equals(searchType) && serviceName != null && !serviceName.isBlank()) {
            resultServices = resultServices.stream()
                    .filter(s -> s.getName().toLowerCase().contains(serviceName.toLowerCase()))
                    .toList();
            model.addAttribute("selectedServiceName", serviceName);
        }
        model.addAttribute("allServices", resultServices);

        return "garage";
    }

    @GetMapping("/dark-garage/{brand}")
    public String getGarageByBrandPath(@PathVariable String brand, Model model) {
        populateBaseGarageModel(model);

        CarResponseDto car = carService.searchCars(brand, null).stream()
                .findFirst()
                .orElseThrow(() -> new CarNotFoundException("Машина марки «" + brand + "» не найдена"));

        model.addAttribute("carsList", List.of(car));
        model.addAttribute("selectedBrand", brand);
        return "garage";
    }

    @GetMapping("/dark-garage/car/{id}")
    public String getCarDetailsPage(@PathVariable Long id, Model model) {
        populateBaseGarageModel(model);

        CarResponseDto car = carService.getCarById(id)
                .orElseThrow(() -> new CarNotFoundException("Машина с id " + id + " не найдена"));

        UserResponseDto owner = car.userId() != null
                ? userService.getUserById(car.userId())
                : null;

        model.addAttribute("car", car);
        model.addAttribute("owner", owner);
        model.addAttribute("servicesList", serviceService.getServicesByCarId(id));

        return "car-details";
    }

    @GetMapping("/dark-garage/delete/{brand}")
    public String deleteCarByBrand(@PathVariable String brand) {
        carService.removeAllByBrand(brand);
        return "redirect:/dark-garage";
    }

    @GetMapping("/manage")
    public String getManagePage(Model model) {
        model.addAttribute("newCar", new Car());
        model.addAttribute("stats", garageManagementService.getGarageStats());
        return "manage";
    }

    private void populateBaseGarageModel(Model model) {
        model.addAttribute("garageTitle", garageManagementService.getGarageTitle());
        model.addAttribute("allBrands", carService.getAllBrands());
        model.addAttribute("garageStatus", garageManagementService.getGarageStatus());
        model.addAttribute("freePlaces", garageManagementService.getFreePlaces());
        model.addAttribute("usersList", userService.getAllUsers());
        model.addAttribute("allServices", serviceService.getAllServices());
    }
}