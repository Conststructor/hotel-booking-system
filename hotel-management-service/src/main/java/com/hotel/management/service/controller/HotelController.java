package com.hotel.management.service.controller;

import com.hotel.management.service.dto.CreateHotelRequest;
import com.hotel.management.service.entity.Hotel;
import com.hotel.management.service.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления отелями.
 */
@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    /**
     * Создает новый отель (ADMIN).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hotel> createHotel(@Valid @RequestBody CreateHotelRequest request) {
        Hotel hotel = hotelService.createHotel(request);
        return ResponseEntity.ok(hotel);
    }

    /**
     * Получает список всех отелей (USER, ADMIN).
     */
    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    /**
     * Получает отель по ID (USER, ADMIN).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable Long id) {
        return hotelService.getHotelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обновляет данные отеля (ADMIN).
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hotel> updateHotel(@PathVariable Long id, 
                                              @Valid @RequestBody CreateHotelRequest request) {
        Hotel hotel = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(hotel);
    }

    /**
     * Удаляет отель по ID (ADMIN).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}
