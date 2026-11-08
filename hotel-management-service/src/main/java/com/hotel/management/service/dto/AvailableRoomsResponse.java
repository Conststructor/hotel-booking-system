package com.hotel.management.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для списка доступных номеров.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableRoomsResponse {

    private Long roomId;
    private String number;
    private String roomType;
    private int maxOccupancy;
    private Integer timesBooked;
}
