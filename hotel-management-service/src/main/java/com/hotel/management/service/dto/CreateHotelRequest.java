package com.hotel.management.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для создания отеля.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHotelRequest {

    private String name;
    private String address;
    private String description;
}
