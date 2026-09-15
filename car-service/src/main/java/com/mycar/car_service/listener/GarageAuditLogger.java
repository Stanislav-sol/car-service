package com.mycar.car_service.listener;

import com.mycar.car_service.event.CarCreatedEvent;
import com.mycar.car_service.event.CarDeletedEvent;
import com.mycar.car_service.event.GarageCapacityAlertEvent;
import com.mycar.car_service.kafka.CarKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class GarageAuditLogger {
    private static final Logger log = LoggerFactory.getLogger(GarageAuditLogger.class);
    private final CarKafkaProducer kafkaProducer;

    public GarageAuditLogger(CarKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Async
    @EventListener
    public void onCarCreated(CarCreatedEvent event) {
        log.info("В гараж добавлена машина ID: {} | {} {} ({} г.)",
                event.carId(), event.brand(), event.model(), event.year());
        kafkaProducer.sendCarCreatedEvent(event);
    }

    @Async
    @EventListener
    public void onCarDelete(CarDeletedEvent event) {
        log.warn("Из гаража удалена машина ID: {}", event.carId());
        kafkaProducer.sendCarDeletedEvent(event);
    }

    @Async
    @EventListener
    public void warnGarageCapacity(GarageCapacityAlertEvent event) {
        log.warn("Гараж почти заполнен, занято {} из {} мест",
                event.currentCapacity(), event.maxCapacity());
    }
}