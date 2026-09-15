package com.mycar.car_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Марка не может быть пустой")
    @Size(min = 2, max = 30, message = "Марка должна быть от 2 до 30 символов")
    @Column(name = "brand", nullable = false, length = 50)
    private String brand;
    @NotBlank(message = "Модель не может быть пустой")
    @Size(min = 1, max = 50, message = "Модель не должна превышать 50 символов")
    @Column(name = "model")
    private String model;
    @NotBlank(message = "Описание стиля не может быть пустой")
    @Size(min = 10, max = 100, message = "Описание должно быть от 10 до 100 символов")
    @Column(name = "design_style", length = 100)
    private String designStyle;
    @Min(value = 1990, message = "Год выпуска не может быть раньше 1990")
    @Max(value = 2026, message = "Год выпуска не может быть из будущего")
    @Column(name = "year")
    private int year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Service> services = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Car() {}

    public Car(String brand, String model, String designStyle, int year) {
        this(null, brand, model, designStyle, year);
    }

    public Car(Long id, String brand, String model, String designStyle, int year) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.designStyle = designStyle;
        this.year = year;
    }

    public Long getId(){return id;}
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel(){return model;}
    public void setModel(String model){ this.model = model; }

    public String getDesignStyle(){return designStyle;}
    public void setDesignStyle(String designStyle) { this.designStyle = designStyle; }

    public int getYear(){ return year; }
    public void setYear(int year){ this.year = year; }
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Service> getServices() { return services; }
    public void setServices(List<Service> services) { this.services = services; }

    public void addService(Service service) {
        services.add(service);
        service.setCar(this);
    }

    public void removeService(Service service) {
        services.remove(service);
        service.setCar(null);
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
        Car car = (Car) o;
        return id != null && id.equals(car.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
