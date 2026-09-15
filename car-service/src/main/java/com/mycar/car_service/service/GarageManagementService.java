package com.mycar.car_service.service;

import com.mycar.car_service.component.CarInspectionTask;
import com.mycar.car_service.component.GarageAsyncMethods;
import com.mycar.car_service.model.Car;
import com.mycar.car_service.model.GarageStats;
import com.mycar.car_service.repository.CarRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GarageManagementService {
    private static final Logger log = LoggerFactory.getLogger(GarageManagementService.class);

    @Value("${garage.status}")
    private String garageStatus;

    @Value("${garage.title}")
    private String garageTitle;

    @Value("${garage.max-capacity}")
    private int maxCapacity;
    private final CarRepository carRepository;
    private final ObjectProvider<CarInspectionTask> inspectionTasks;
    private final GarageAsyncMethods garageAsyncMethods;

    public GarageManagementService(CarRepository carRepository,
                                   ObjectProvider<CarInspectionTask> inspectionTasks,
                                   GarageAsyncMethods garageAsyncMethods) {
        this.carRepository = carRepository;
        this.inspectionTasks = inspectionTasks;
        this.garageAsyncMethods = garageAsyncMethods;
    }

    @PostConstruct
    public void init() {
        log.info("Инициализация гаража: '{}'", garageTitle);
        log.info("Максимальная вместимость: {} мест", maxCapacity);
        CarInspectionTask task = inspectionTasks.getObject();

        try {
            garageAsyncMethods.sendAsyncReport();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<Car> cars = carRepository.findAll();
        cars.forEach(car -> task.inspect(car.getId()));
        log.info("Автопарк успешно загружен из БД. Всего машин: {}", cars.size());
    }

    public GarageStats getGarageStats() {
        long totalCars = carRepository.count();
        Integer maxYear = carRepository.findMaxYear();
        Integer minYear = carRepository.findMinYear();
        int newestYear = maxYear != null ? maxYear : 0;
        int oldestYear = minYear != null ? minYear : 0;
        int availableSpots = Math.max(0, maxCapacity - (int) totalCars);
        List<String> uniqueBrands = carRepository.findDistinctBrands();

        return new GarageStats((int) totalCars, newestYear, oldestYear, availableSpots, uniqueBrands);
    }

    public int getFreePlaces() {
        long currentCount = carRepository.count();
        return Math.max(0, maxCapacity - (int) currentCount);
    }

    public String getGarageStatus() { return garageStatus; }
    public String getGarageTitle() { return garageTitle; }
    public int getMaxCapacity() { return maxCapacity; }
}