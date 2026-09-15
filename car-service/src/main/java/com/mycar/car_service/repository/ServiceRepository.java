package com.mycar.car_service.repository;

import com.mycar.car_service.model.Car;
import com.mycar.car_service.model.Service;
import com.mycar.car_service.model.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {
    @Query("SELECT s FROM Service s WHERE s.price < :price")
    List<Service> findLowerThenPrice(@Param("price") BigDecimal price);

    List<Service> getServicesByCarId(Long id);

    @Query("SELECT s FROM Service s WHERE s.price > :price")
    List<Service> findUpperThenPrice(@Param("price") BigDecimal price);

    @Query("SELECT MAX(s.price) FROM Service s")
    BigDecimal findMaxPrice();

    @Query("SELECT MIN(s.price) FROM Service s")
    BigDecimal findMinPrice();

    List<Service> findByStatus(ServiceStatus status);

    List<Service> findByExecutionTimeLessThanEqualAndStatus(int executionTime, ServiceStatus status);

    @Query("SELECT SUM(s.price) FROM Service s WHERE s.car.id = :carId")
    BigDecimal findTotalPriceByCarId(@Param("carId") Long carId);

    @Query("SELECT DISTINCT c FROM Car c JOIN c.services s WHERE s.status = :status")
    List<Car> findCarsByServiceStatus(@Param("status") ServiceStatus status);
}
