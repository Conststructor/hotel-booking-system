package com.hotel.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовая конфигурация тестов для Booking Service.
 */
@SpringBootTest
@ActiveProfiles("test")
public class BookingServiceTests {

    @Test
    public void contextLoads() {
        // Проверка загрузки контекста Spring Application
    }
}
