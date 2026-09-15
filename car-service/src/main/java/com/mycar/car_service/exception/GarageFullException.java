package com.mycar.car_service.exception;

public class GarageFullException extends RuntimeException{
    public GarageFullException (String message){
        super(message);
    }
}
