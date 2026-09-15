package com.mycar.car_service.kafka;

import com.mycar.car_service.event.CarCreatedEvent;
import com.mycar.car_service.event.CarDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CarKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(CarKafkaConsumer.class);

    @KafkaListener(topics = "car-created-topic", groupId = "car-service-group")
    public void consumeCarCreated(CarCreatedEvent event) {
        log.info("Получено событие из Kafka: Машина создана -> ID: {}, {} {}",
                event.carId(), event.brand(), event.model());
    }

    @KafkaListener(topics = "car-deleted-topic", groupId = "car-service-group")
    public void consumeCarDeleted(CarDeletedEvent event) {
        log.info("Получено событие из Kafka: Машина удалена -> ID: {}",
                event.carId());
    }
}