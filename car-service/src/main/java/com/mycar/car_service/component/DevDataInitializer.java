package com.mycar.car_service.component;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    @Override
    public void run(String... args) {
        log.info("Среда разработки готова. Инициализация тестового профиля...");
    }
}
