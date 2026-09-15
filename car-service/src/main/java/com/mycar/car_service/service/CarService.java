package com.mycar.car_service.service;

import com.mycar.car_service.dto.CarListResponseDto;
import com.mycar.car_service.dto.CarResponseDto;
import com.mycar.car_service.event.CarCreatedEvent;
import com.mycar.car_service.event.CarDeletedEvent;
import com.mycar.car_service.event.GarageCapacityAlertEvent;
import com.mycar.car_service.exception.CarNotFoundException;
import com.mycar.car_service.model.Car;
import com.mycar.car_service.model.User;
import com.mycar.car_service.repository.CarRepository;
import com.mycar.car_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CarService {
    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    @Value("${garage.max-capacity:10}")
    private int maxCapacity;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CarService(CarRepository carRepository,
                      UserRepository userRepository,
                      ApplicationEventPublisher eventPublisher) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Cacheable(value = "cars", key = "'all_cars'")
    public CarListResponseDto getAllCars() {
        List<CarResponseDto> cars = carRepository.findAll().stream()
                .map(this::mapToDto) // ваша логика маппинга
                .toList();
        return new CarListResponseDto(cars);
    }

    public List<CarResponseDto> getCars() {
        log.info("Запрос списка машин");
        return carRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public Optional<CarResponseDto> getCarById(Long id) {
        return carRepository.findById(id).map(this::mapToDto);
    }

    public Optional<Car> getCarEntityById(Long id) {
        return carRepository.findById(id);
    }

    public List<CarResponseDto> getCarsByMinYear(int minYear) {
        return carRepository.findByYearGreaterThanEqual(minYear).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "cars", allEntries = true)
    public CarResponseDto addCar(Car car) {
        Car savedCar = carRepository.save(car);
        log.info("Добавлена новая машина в БД: id = {}, brand = {}", savedCar.getId(), savedCar.getBrand());

        eventPublisher.publishEvent(new CarCreatedEvent(
                savedCar.getId(),
                savedCar.getBrand(),
                savedCar.getModel(),
                savedCar.getYear(),
                LocalDateTime.now()
        ));
        long currentCount = carRepository.count();

        if (currentCount >= maxCapacity - 1) {
            eventPublisher.publishEvent(new GarageCapacityAlertEvent(maxCapacity, (int) currentCount));
        }

        return mapToDto(savedCar);
    }

    @Transactional
    @CacheEvict(value = "cars", allEntries = true)
    public CarResponseDto addCarWithUser(Car car, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь с id " + userId + " не найден"));
            car.setUser(user);
        }
        return addCar(car);
    }

    @Transactional
    @CacheEvict(value = "cars", allEntries = true)
    public CarResponseDto updateCar(Long id, Car updatedCar) {
        Car existingCar = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Машина с id " + id + " не найдена для обновления"));

        existingCar.setBrand(updatedCar.getBrand());
        existingCar.setModel(updatedCar.getModel());
        existingCar.setDesignStyle(updatedCar.getDesignStyle());
        existingCar.setYear(updatedCar.getYear());

        log.info("Машина с id = {} успешно обновлена", id);
        return mapToDto(carRepository.save(existingCar));
    }

    @Transactional
    @CacheEvict(value = "cars", allEntries = true)
    public boolean deleteCarById(Long id) {
        return carRepository.findById(id).map(car -> {
            eventPublisher.publishEvent(new CarDeletedEvent(
                    car.getId(),
                    car.getBrand(),
                    car.getModel(),
                    car.getYear(),
                    LocalDateTime.now()
            ));
            carRepository.delete(car);
            log.info("Машина с id = {} удалена", id);
            return true;
        }).orElse(false);
    }

    @Transactional
    @CacheEvict(value = "cars", allEntries = true)
    public boolean removeAllByBrand(String brand) {
        List<Car> carsByBrand = carRepository.findByBrandIgnoreCase(brand);

        if (!carsByBrand.isEmpty()) {
            carRepository.deleteAll(carsByBrand);
            log.info("Машины бренда: '{}' удалены, кол-во: {}", brand, carsByBrand.size());
            return true;
        }
        return false;
    }

    public List<CarResponseDto> searchCars(String brand, String model) {
        boolean hasBrand = brand != null && !brand.isBlank();
        boolean hasModel = model != null && !model.isBlank();
        List<Car> cars;

        if (hasBrand && hasModel) {
            cars = carRepository.findByBrandIgnoreCaseAndModelIgnoreCase(brand, model);
        } else if (hasBrand) {
            cars = carRepository.findByBrandIgnoreCase(brand);
        } else if (hasModel) {
            cars = carRepository.findByModelIgnoreCase(model);
        } else {
            cars = carRepository.findAll();
        }

        return cars.stream().map(this::mapToDto).toList();
    }

    public List<CarResponseDto> getEarliestCars() {
        return carRepository.findOldestCars().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<String> getAllBrands() {
        return carRepository.findDistinctBrands();
    }

    public CarResponseDto mapToDto(Car car) {
        return new CarResponseDto(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getDesignStyle(),
                car.getYear(),
                car.getUser() != null ? car.getUser().getId() : null,
                car.getCreatedAt(),
                car.getUpdatedAt()
        );
    }
}