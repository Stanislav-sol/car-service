package com.mycar.car_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic carCreatedTopic() {
        return TopicBuilder.name("car-created-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic carDeletedTopic() {
        return TopicBuilder.name("car-deleted-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}