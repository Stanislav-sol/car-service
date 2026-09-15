package com.mycar.car_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Время должно быть указано")
    @Min(value = 1, message = "Время выполнения должно быть не менее 1 минуты")
    @Column(name = "execution_time", nullable = false)
    private int executionTime;

    @NotNull(message = "Цена должна быть указана")
    @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Статус должен быть указан")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    @JsonIgnore
    private Car car;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Service() {}

    public Service(String name, int executionTime, BigDecimal price, ServiceStatus status, Car car){
        this.name = name;
        this.executionTime = executionTime;
        this.price = price;
        this.status = status;
        this.car = car;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return id != null && id.equals(service.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
