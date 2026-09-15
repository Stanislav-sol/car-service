package com.mycar.car_service.service;

import com.mycar.car_service.dto.ServiceRequestDto;
import com.mycar.car_service.dto.ServiceResponseDto;
import com.mycar.car_service.exception.CarNotFoundException;
import com.mycar.car_service.model.Car;
import com.mycar.car_service.model.Service;
import com.mycar.car_service.model.ServiceStatus;
import com.mycar.car_service.repository.CarRepository;
import com.mycar.car_service.repository.ServiceRepository;
import com.mycar.car_service.repository.ServiceSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final CarRepository carRepository;

    public ServiceService(ServiceRepository serviceRepository, CarRepository carRepository) {
        this.serviceRepository = serviceRepository;
        this.carRepository = carRepository;
    }

    public List<ServiceResponseDto> getAllServices(BigDecimal minPrice, BigDecimal maxPrice, ServiceStatus status, String carBrand) {
        Specification<Service> spec = ServiceSpecification.filterServices(minPrice, maxPrice, status, carBrand);
        return serviceRepository.findAll(spec).stream()
                .map(this::mapToDto)
                .toList();
    }

    public ServiceResponseDto getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена с id: " + id));
        return mapToDto(service);
    }

    public List<ServiceResponseDto> getAllServices() {
        return getAllServices(null, null, null, null);
    }

    public List<ServiceResponseDto> getServicesByCarId(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new CarNotFoundException("Машина с id " + carId + " не найдена");
        }
        return serviceRepository.getServicesByCarId(carId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public ServiceResponseDto createServiceForCar(Long carId, ServiceRequestDto request) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException("Машина с id " + carId + " не найдена"));

        Service service = new Service(
                request.getName(),
                request.getExecutionTime(),
                request.getPrice(),
                request.getStatus() != null ? request.getStatus() : ServiceStatus.PENDING,
                car
        );

        Service savedService = serviceRepository.save(service);
        return mapToDto(savedService);
    }

    @Transactional
    public ServiceResponseDto createService(ServiceRequestDto request) {
        return createServiceForCar(request.getCarId(), request);
    }

    @Transactional
    public ServiceResponseDto updateService(Long id, ServiceRequestDto request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена с id: " + id));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new CarNotFoundException("Машина с id " + request.getCarId() + " не найдена"));

        service.setName(request.getName());
        service.setExecutionTime(request.getExecutionTime());
        service.setPrice(request.getPrice());
        service.setStatus(request.getStatus());
        service.setCar(car);

        return mapToDto(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new RuntimeException("Услуга не найдена с id: " + id);
        }
        serviceRepository.deleteById(id);
    }

    private ServiceResponseDto mapToDto(Service service) {
        return new ServiceResponseDto(
                service.getId(),
                service.getName(),
                service.getExecutionTime(),
                service.getPrice(),
                service.getStatus(),
                service.getCar() != null ? service.getCar().getId() : null,
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}