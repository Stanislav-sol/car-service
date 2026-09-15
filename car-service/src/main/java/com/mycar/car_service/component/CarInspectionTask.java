package com.mycar.car_service.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class CarInspectionTask {
    private static final Logger log = LoggerFactory.getLogger(CarInspectionTask.class);
    private String taskId;

    public void inspect(Long carId){
        taskId = UUID.randomUUID().toString();
        log.info("ID задачи = '{}', ID машины = {}", taskId, carId);
    }
}
