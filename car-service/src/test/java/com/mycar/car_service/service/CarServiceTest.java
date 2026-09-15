package com.mycar.car_service.service;

import com.mycar.car_service.dto.CarResponseDto;
import com.mycar.car_service.event.CarCreatedEvent;
import com.mycar.car_service.event.CarDeletedEvent;
import com.mycar.car_service.event.GarageCapacityAlertEvent;
import com.mycar.car_service.exception.CarNotFoundException;
import com.mycar.car_service.model.Car;
import com.mycar.car_service.model.User;
import com.mycar.car_service.repository.CarRepository;
import com.mycar.car_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CarService carService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(carService, "maxCapacity", 10);
    }

    @Test
    @DisplayName("Добавление машины с сохранением в репозиторий и публикацией события")
    void addCar_ShouldSaveAndPublishEvent() {
        Car car = new Car("Changan", "UNI-V", "Динамичный силуэт", 2023);
        Car savedCar = new Car("Changan", "UNI-V", "Динамичный силуэт", 2023);
        savedCar.setId(1L);

        when(carRepository.save(car)).thenReturn(savedCar);
        when(carRepository.count()).thenReturn(1L);

        CarResponseDto result = carService.addCar(car);

        assertNotNull(result.id(), "ID не должен быть null после сохранения");
        assertEquals(1L, result.id());
        assertEquals("Changan", result.brand());

        verify(carRepository, times(1)).save(car);
        verify(eventPublisher, times(1)).publishEvent(any(CarCreatedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(GarageCapacityAlertEvent.class));
    }

    @Test
    @DisplayName("Публикация события о заполнении гаража при достижении лимита")
    void addCar_ShouldPublishCapacityAlert_WhenLimitReached() {
        Car car = new Car("Changan", "UNI-V", "Динамичный силуэт", 2023);
        Car savedCar = new Car("Changan", "UNI-V", "Динамичный силуэт", 2023);
        savedCar.setId(10L);

        when(carRepository.save(car)).thenReturn(savedCar);
        when(carRepository.count()).thenReturn(9L);

        carService.addCar(car);

        verify(eventPublisher, times(1)).publishEvent(any(CarCreatedEvent.class));
        verify(eventPublisher, times(1)).publishEvent(any(GarageCapacityAlertEvent.class));
    }

    @Test
    @DisplayName("Добавление машины с привязкой существующего пользователя")
    void addCarWithUser_ShouldAttachUser_WhenUserExists() {
        Car car = new Car("Changan", "UNI-V", "Динамичный силуэт", 2023);
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(carRepository.save(car)).thenAnswer(invocation -> invocation.getArgument(0));

        CarResponseDto result = carService.addCarWithUser(car, 1L);

        assertEquals(1L, result.userId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Выброс IllegalArgumentException при привязке несуществующего пользователя")
    void addCarWithUser_ShouldThrowException_WhenUserNotFound() {
        Car car = new Car("Changan", "UNI-V", "Динамичный силуэт", 2023);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carService.addCarWithUser(car, 999L)
        );

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Поиск существующей машины по ID")
    void getCarById_ShouldReturnCar_WhenCarExists() {
        Car car = new Car("Audi", "RS7", "Широкий кузов", 2022);
        car.setId(1L);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        Optional<CarResponseDto> foundCar = carService.getCarById(1L);

        assertTrue(foundCar.isPresent(), "Машина должна быть найдена");
        assertEquals("Audi", foundCar.get().brand());
        assertEquals("RS7", foundCar.get().model());
    }

    @Test
    @DisplayName("Возврат пустой Optional, если машина с ID не найдена")
    void getCarById_ShouldReturnEmpty_WhenCarDoesNotExist() {
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CarResponseDto> foundCar = carService.getCarById(999L);

        assertTrue(foundCar.isEmpty(), "Для несуществующего ID Optional должен быть пустым");
    }

    @Test
    @DisplayName("Успешное обновление данных машины")
    void updateCar_ShouldUpdateFields_WhenCarExists() {
        Car existingCar = new Car("Peugeot", "508", "Агрессивные диоды", 2022);
        existingCar.setId(1L);

        Car updateInfo = new Car("Peugeot", "508 PSE", "Гибридный спортбэк", 2024);

        when(carRepository.findById(1L)).thenReturn(Optional.of(existingCar));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarResponseDto updatedCar = carService.updateCar(1L, updateInfo);

        assertEquals("508 PSE", updatedCar.model());
        assertEquals("Гибридный спортбэк", updatedCar.designStyle());
        assertEquals(2024, updatedCar.year());

        verify(carRepository, times(1)).save(existingCar);
    }

    @Test
    @DisplayName("Выброс CarNotFoundException при попытке обновить несуществующую машину")
    void updateCar_ShouldThrowException_WhenCarNotFound() {
        Car updateInfo = new Car("Acura", "TLX", "Широкая база", 2021);
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        CarNotFoundException exception = assertThrows(
                CarNotFoundException.class,
                () -> carService.updateCar(999L, updateInfo)
        );

        assertEquals("Машина с id 999 не найдена для обновления", exception.getMessage());
    }

    @Test
    @DisplayName("Успешное удаление машины по ID с публикацией события")
    void deleteCarById_ShouldRemoveCarAndPublishEvent() {
        Car car = new Car("Acura", "Integra", "Спортивный лифтбек", 2024);
        car.setId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        boolean isDeleted = carService.deleteCarById(1L);

        assertTrue(isDeleted, "Метод должен вернуть true при успешном удалении");
        verify(carRepository, times(1)).delete(car);
        verify(eventPublisher, times(1)).publishEvent(any(CarDeletedEvent.class));
    }

    @Test
    @DisplayName("Возврат false при попытке удалить несуществующую машину по ID")
    void deleteCarById_ShouldReturnFalse_WhenNotFound() {
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        boolean isDeleted = carService.deleteCarById(999L);

        assertFalse(isDeleted);
        verify(carRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Успешное удаление машин по бренду")
    void removeAllByBrand_ShouldReturnTrue_WhenCarsExist() {
        Car car1 = new Car("Ferrari", "SF90", "Суперкар", 2023);
        when(carRepository.findByBrandIgnoreCase("Ferrari")).thenReturn(List.of(car1));

        boolean result = carService.removeAllByBrand("Ferrari");

        assertTrue(result);
        verify(carRepository, times(1)).deleteAll(List.of(car1));
    }

    @Test
    @DisplayName("Возврат false при попытке удалить машины несуществующего бренда")
    void removeAllByBrand_ShouldReturnFalse_WhenBrandNotFound() {
        when(carRepository.findByBrandIgnoreCase("Ferrari")).thenReturn(Collections.emptyList());

        boolean result = carService.removeAllByBrand("Ferrari");

        assertFalse(result);
        verify(carRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("Поиск машин по бренду и модели")
    void searchCars_ShouldReturnFilteredList() {
        Car c1 = new Car("Changan", "UNI-V", "Описание 1", 2023);
        Car c2 = new Car("Changan", "UNI-K", "Описание 2", 2023);

        when(carRepository.findByBrandIgnoreCase("Changan")).thenReturn(List.of(c1, c2));
        when(carRepository.findByBrandIgnoreCaseAndModelIgnoreCase("Changan", "UNI-K"))
                .thenReturn(List.of(c2));

        List<CarResponseDto> changanResult = carService.searchCars("Changan", null);
        List<CarResponseDto> exactModelResult = carService.searchCars("Changan", "UNI-K");

        assertEquals(2, changanResult.size());
        assertEquals(1, exactModelResult.size());
        assertEquals("UNI-K", exactModelResult.get(0).model());
    }
}