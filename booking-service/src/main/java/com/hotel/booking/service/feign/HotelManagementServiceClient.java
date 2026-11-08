package com.hotel.booking.service.feign;

import com.hotel.management.service.dto.AvailableRoomsResponse;
import com.hotel.management.service.dto.RoomAvailabilityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client для вызова Hotel Management Service.
 */
@FeignClient(name = "hotel-management-service", url = "${eureka.client.service-url.defaultZone:}")
public interface HotelManagementServiceClient {

    /**
     * Подтверждает доступность номера на запрошенные даты.
     */
    @PostMapping("/api/rooms/{roomId}/confirm-availability")
    boolean confirmAvailability(@PathVariable("roomId") Long roomId,
                                @RequestBody RoomAvailabilityRequest request);

    /**
     * Снимает временную блокировку слота (компенсация при сбое).
     */
    @PostMapping("/api/rooms/{roomId}/release")
    void releaseRoom(@PathVariable("roomId") Long roomId);

    /**
     * Счётчик бронирований для номера.
     */
    @PostMapping("/api/rooms/{roomId}/increment-booking-count")
    void incrementBookingCount(@PathVariable("roomId") Long roomId);

    /**
     * Список рекомендованных номеров (для автовыбора).
     */
    @GetMapping("/api/rooms/recommend")
    AvailableRoomsResponse[] getRecommendedRooms();

    /**
     * Получает доступный номер по ID.
     */
    @GetMapping("/api/rooms/{roomId}")
    AvailableRoomsResponse getRoomById(@PathVariable("roomId") Long roomId);
}
