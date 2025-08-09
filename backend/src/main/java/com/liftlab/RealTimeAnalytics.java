package com.liftlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.liftlab"})
public class RealTimeAnalytics {
    public static void main(String[] args) {
        SpringApplication.run(RealTimeAnalytics.class, args);
    }
}