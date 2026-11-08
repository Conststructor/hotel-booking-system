package com.hotel.booking.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для списка бронирований пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingsResponse {

    private Long bookingId;
    private Long roomId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private LocalDateTime createdAt;
}
