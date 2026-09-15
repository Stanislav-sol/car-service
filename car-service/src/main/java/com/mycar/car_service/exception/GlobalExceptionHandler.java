package com.mycar.car_service.exception;

import com.mycar.car_service.controller.GarageViewController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = GarageViewController.class)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CarNotFoundException.class)
    public String handleCarNotFound(CarNotFoundException ex, Model model){
        log.warn("Перехвачено ошибка CarNotFoundException: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "garage";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model){
        log.error("Перехвачено необработаное исключение: ", ex);
        model.addAttribute("errorMessage", "Произошла системная ошибка: " + ex.getMessage());
        return "garage";
    }
}
