package com.mycar.car_service.bpp;

import com.mycar.car_service.service.CarService;
import com.mycar.car_service.service.GarageManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomGarageBeanPostProcessor implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(CustomGarageBeanPostProcessor.class);
    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof GarageManagementService || bean instanceof CarService) {
            startTimes.put(beanName, System.currentTimeMillis());
            log.info("Перехвачен бин '{}'. Подготовка к вызову @PostConstruct...", beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (startTimes.containsKey(beanName)) {
            long duration = System.currentTimeMillis() - startTimes.remove(beanName);
            log.info("Инициализация '{}' завершена за '{}' мс", beanName, duration);
            log.info("Бин '{}' успешно прошёл @PostConstruct", beanName);
        }
        return bean;
    }
}
