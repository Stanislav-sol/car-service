package com.mycar.car_service.repository;

import com.mycar.car_service.model.Service;
import com.mycar.car_service.model.ServiceStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ServiceSpecification {

    public static Specification<Service> filterServices(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ServiceStatus status,
            String carBrand) {

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (minPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("status"), status));
            }

            if (carBrand != null && !carBrand.isBlank()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.join("car").get("brand")),
                                carBrand.toLowerCase()
                        ));
            }

            return predicate;
        };
    }
}