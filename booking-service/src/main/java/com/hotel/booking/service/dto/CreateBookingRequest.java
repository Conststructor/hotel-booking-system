package com.hotel.booking.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для создания бронирования.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    private Long roomId;
    private String startDateTime;
    private String endDateTime;
    private boolean autoSelect = false;    // Если true, сервис сам выберет номер

    /**
     * Корреляционный идентификатор запроса (для идемпотентности).
     */
    private String requestId;
}
