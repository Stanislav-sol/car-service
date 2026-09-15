package com.mycar.car_service.exception;

public class CarNotFoundException extends RuntimeException{
    public CarNotFoundException(String message){
        super(message);
    }
}
