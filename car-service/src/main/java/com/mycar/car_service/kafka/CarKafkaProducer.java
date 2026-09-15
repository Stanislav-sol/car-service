package com.mycar.car_service.kafka;

import com.mycar.car_service.event.CarCreatedEvent;
import com.mycar.car_service.event.CarDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CarKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(CarKafkaProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CarKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCarCreatedEvent(CarCreatedEvent event) {
        log.info("Отправка события создания машины в Kafka: ID {}", event.carId());
        kafkaTemplate.send("car-created-topic", String.valueOf(event.carId()), event);
    }

    public void sendCarDeletedEvent(CarDeletedEvent event) {
        log.info("Отправка события удаления машины в Kafka: ID {}", event.carId());
        kafkaTemplate.send("car-deleted-topic", String.valueOf(event.carId()), event);
    }
}