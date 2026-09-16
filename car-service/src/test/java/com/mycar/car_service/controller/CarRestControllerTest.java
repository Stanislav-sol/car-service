package com.mycar.car_service.controller;

import com.mycar.car_service.dto.CarResponseDto;
import com.mycar.car_service.exception.CarNotFoundException;
import com.mycar.car_service.service.CarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarRestControllerTest {

    @Mock
    private CarService carService;

    @InjectMocks
    private CarRestController carRestController;

    @Test
    @DisplayName("Проверка корректного поиска машины по ID")
    void getCarById_ShouldReturn200AndCar_WhenCarExists() {
        CarResponseDto dto = new CarResponseDto(
                1L, "Changan", "UNI-V", "Динамичный силуэт", 2023, null, LocalDateTime.now(), LocalDateTime.now()
        );
        when(carService.getCarById(1L)).thenReturn(Optional.of(dto));

        ResponseEntity<CarResponseDto> response = carRestController.getCarById(1L);

        assertEquals(200, response.getStatusCode().value(), "Статус должен быть 200");
        assertNotNull(response.getBody(), "Тело ответа не должно быть null");
        assertEquals("Changan", response.getBody().brand());

        verify(carService, times(1)).getCarById(1L);
    }

    @Test
    @DisplayName("Проверка на выброс ошибки при запросе несуществующей машины")
    void getCarById_ShouldThrowException_WhenCarNotFound() {
        when(carService.getCarById(999L)).thenReturn(Optional.empty());

        CarNotFoundException exception = assertThrows(
                CarNotFoundException.class,
                () -> carRestController.getCarById(999L),
                "При запросе несуществующей машины должно выбрасываться исключение"
        );

        assertEquals("Машина с id 999 не найдена", exception.getMessage());
        verify(carService, times(1)).getCarById(999L);
    }
}
