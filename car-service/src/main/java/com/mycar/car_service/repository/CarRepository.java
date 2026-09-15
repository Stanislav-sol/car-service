package com.mycar.car_service.repository;

import com.mycar.car_service.model.Car;
import com.mycar.car_service.model.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByBrandIgnoreCase(String brand);

    List<Car> findByModelIgnoreCase(String model);

    List<Car> findByBrandIgnoreCaseAndModelIgnoreCase(String brand, String model);

    Optional<Car> findFirstByBrandIgnoreCase(String brand);

    long countByBrandIgnoreCase(String brand);

    List<Car> findByYearGreaterThanEqual(int minYear);

    @Query("SELECT MAX(c.year) FROM Car c")
    Integer findMaxYear();

    @Query("SELECT MIN(c.year) FROM Car c")
    Integer findMinYear();

    @Query("SELECT c FROM Car c WHERE c.year = (SELECT MIN(c2.year) FROM Car c2)")
    List<Car> findOldestCars();

    @Query("SELECT DISTINCT c.brand FROM Car c")
    List<String> findDistinctBrands();

    @Query("SELECT DISTINCT c FROM Car c JOIN c.services s WHERE s.status = :status")
    List<Car> findCarsByServiceStatus(@Param("status") ServiceStatus status);

    @Query("SELECT DISTINCT c FROM Car c LEFT JOIN FETCH c.user")
    List<Car> findAllWithUser();
}