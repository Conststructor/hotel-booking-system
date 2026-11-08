package com.hotel.management.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для создания номера.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    private Long hotelId;
    private String number;
    private String roomType;
    private int maxOccupancy;
}
