package com.hotel.management.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для подтверждения доступности номера (внутреннее использование).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityRequest {

    private Long roomId;
    private String startDateTime;
    private String endDateTime;
}
