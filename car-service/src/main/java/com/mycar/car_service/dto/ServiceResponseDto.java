package com.mycar.car_service.dto;

import com.mycar.car_service.model.ServiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceResponseDto {
    private Long id;
    private String name;
    private int executionTime;
    private BigDecimal price;
    private ServiceStatus status;
    private Long carId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServiceResponseDto() {}

    public ServiceResponseDto(Long id, String name, int executionTime, BigDecimal price, ServiceStatus status, Long carId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.executionTime = executionTime;
        this.price = price;
        this.status = status;
        this.carId = carId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getExecutionTime() { return executionTime; }
    public void setExecutionTime(int executionTime) { this.executionTime = executionTime; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public ServiceStatus getStatus() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }
    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}