package com.mycar.car_service.controller;

import com.mycar.car_service.service.CarService;
import com.mycar.car_service.service.GarageManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class GarageViewControllerTest {

    @Mock
    private CarService carService;

    @Mock
    private GarageManagementService garageManagementService;

    @InjectMocks
    private GarageViewController garageViewController;

    @Test
    @DisplayName("Проверка на возврат точной страницы manage")
    void getManagePage_ShouldReturnManageView() {
        Model model = mock(Model.class);

        String viewName = garageViewController.getManagePage(model);

        assertEquals("manage", viewName, "Метод должен возвращать шаблон 'manage'");
    }
}