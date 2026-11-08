package com.hotel.management.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Hotel Management Service - Микросервис для управления отелями и номерами.
 * Отвечает за CRUD операций с отелями, номерами и агрегации по загруженности.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class HotelManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelManagementServiceApplication.class, args);
    }
}
