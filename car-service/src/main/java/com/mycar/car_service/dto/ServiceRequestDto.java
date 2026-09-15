package com.mycar.car_service.dto;

import com.mycar.car_service.model.ServiceStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ServiceRequestDto {
    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @Min(1)
    private Integer executionTime;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    private ServiceStatus status;

    @NotNull
    private Long carId;

    public ServiceRequestDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getExecutionTime() { return executionTime; }
    public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
}