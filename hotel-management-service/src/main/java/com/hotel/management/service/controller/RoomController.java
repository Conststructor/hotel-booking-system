package com.hotel.management.service.controller;

import com.hotel.management.service.dto.AvailableRoomsResponse;
import com.hotel.management.service.dto.CreateRoomRequest;
import com.hotel.management.service.dto.RoomAvailabilityRequest;
import com.hotel.management.service.entity.Room;
import com.hotel.management.service.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления номерами.
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * Создает новый номер в отеле (ADMIN).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Room room = roomService.createRoom(request);
        return ResponseEntity.ok(room);
    }

    /**
     * Получает список доступных номеров (USER, ADMIN).
     */
    @GetMapping
    public ResponseEntity<List<AvailableRoomsResponse>> getAvailableRooms() {
        List<AvailableRoomsResponse> rooms = roomService.getAvailableRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Получает список рекомендованных номеров (USER, ADMIN).
     * Отсортированы по возрастанию times_booked.
     */
    @GetMapping("/recommend")
    public ResponseEntity<List<AvailableRoomsResponse>> getRecommendedRooms() {
        List<AvailableRoomsResponse> rooms = roomService.getRecommendedRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Получает номер по ID (USER, ADMIN).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Подтверждает доступность номера на запрошенные даты (INTERNAL - используется в 2-phase commit).
     */
    @PostMapping("/{id}/confirm-availability")
    public ResponseEntity<Boolean> confirmAvailability(
            @PathVariable Long id, 
            @Valid @RequestBody RoomAvailabilityRequest request) {
        boolean available = roomService.confirmAvailability(request);
        return ResponseEntity.ok(available);
    }

    /**
     * Снимает временную блокировку слота (INTERNAL - компенсация при сбое).
     */
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseRoom(@PathVariable Long id) {
        roomService.releaseRoom(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Увеличивает счётчик бронирований для номера (INTERNAL - вызывается после успешного подтверждения).
     */
    @PostMapping("/{id}/increment-booking-count")
    public ResponseEntity<Void> incrementBookingCount(@PathVariable Long id) {
        roomService.incrementBookingCount(id);
        return ResponseEntity.noContent().build();
    }
}
