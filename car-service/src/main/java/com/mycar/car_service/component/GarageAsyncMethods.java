package com.mycar.car_service.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class GarageAsyncMethods {
    private static final Logger log = LoggerFactory.getLogger(GarageAsyncMethods.class);
    @Async
    public void sendAsyncReport() throws InterruptedException {
        Thread.sleep(3000);
        log.info("Имитируем бурную деятельность с потоком {}", Thread.currentThread().getName());
    }
}
