package com.mycar.car_service.config;

import com.mycar.car_service.interceptor.RequestLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminWebConfig implements WebMvcConfigurer {
    private final RequestLoggingInterceptor requestLoggingInterceptor;

    public AdminWebConfig(RequestLoggingInterceptor requestLoggingInterceptor){
        this.requestLoggingInterceptor = requestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/v1/admin/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico", "/api/v1/admin/login");
    }
}
