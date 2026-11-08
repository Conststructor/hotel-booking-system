package com.hotel.management.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Сущность Номер - информация о номере в отеле.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType;

    @Column(nullable = false)
    private int maxOccupancy;

    @Column(nullable = false)
    private boolean available = true;

    /**
     * Счётчик количества бронирований (для алгоритма равномерной загрузки).
     * Используется для сортировки при рекомендациях.
     */
    @Column(name = "times_booked", nullable = false)
    private int timesBooked = 0;

    public Room(Long hotelId, String number, String roomType, int maxOccupancy) {
        this.hotelId = hotelId;
        this.number = number;
        this.roomType = roomType;
        this.maxOccupancy = maxOccupancy;
        this.available = true;
    }

    /**
     * Увеличивает счётчик бронирований.
     */
    public void incrementTimesBooked() {
        this.timesBooked++;
    }
}
