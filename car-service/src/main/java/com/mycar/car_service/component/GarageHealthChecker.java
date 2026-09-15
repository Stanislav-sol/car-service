package com.mycar.car_service.component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class GarageHealthChecker {
    private static final Logger log = LoggerFactory.getLogger(GarageHealthChecker.class);
    private final Environment env;
    private final int maxCapacity;

    public GarageHealthChecker(Environment env, @Value("${garage.max-capacity}") int maxCapacity) {
        this.env = env;
        this.maxCapacity = maxCapacity;
    }

    @PostConstruct
    public void init() {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        if (isProd) {
            log.info("Проверка пройдена: приложение запущено в PROD профиле, лимит: {}", maxCapacity);
        } else {
            String activeProfiles = env.getActiveProfiles().length > 0
                    ? String.join(", ", env.getActiveProfiles())
                    : "default";
            log.info("Проверка пройдена: активный профиль [{}], лимит мест: {}", activeProfiles, maxCapacity);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Остановка компонентов проверки гаража");
    }
}