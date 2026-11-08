package com.hotel.booking.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Booking Service - Микросервис для управления бронированиями (для тестов).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BookingServiceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceTestApplication.class, args);
    }
}
