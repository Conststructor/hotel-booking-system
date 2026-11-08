package com.hotel.management.service.service;

import com.hotel.management.service.dto.AvailableRoomsResponse;
import com.hotel.management.service.dto.CreateRoomRequest;
import com.hotel.management.service.dto.RoomAvailabilityRequest;
import com.hotel.management.service.entity.Room;
import com.hotel.management.service.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service для работы с номерами.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Создает новый номер в отеле.
     */
    @Transactional
    public Room createRoom(CreateRoomRequest request) {
        Room room = new Room(
                request.getHotelId(),
                request.getNumber(),
                request.getRoomType(),
                request.getMaxOccupancy()
        );
        return roomRepository.save(room);
    }

    /**
     * Получает список доступных номеров.
     */
    @Transactional(readOnly = true)
    public List<AvailableRoomsResponse> getAvailableRooms() {
        List<Room> rooms = roomRepository.findByAvailableTrue();
        return rooms.stream().map(this::toResponse).toList();
    }

    /**
     * Получает список рекомендованных номеров (отсортированный по возрастанию times_booked).
     */
    @Transactional(readOnly = true)
    public List<AvailableRoomsResponse> getRecommendedRooms() {
        List<Room> rooms = roomRepository.findByAvailableTrue();
        
        // Сортируем по возрастанию times_booked (меньше бронирований = больше рекомендуется)
        rooms.sort((r1, r2) -> {
            int cmp = Integer.compare(r1.getTimesBooked(), r2.getTimesBooked());
            if (cmp != 0) return cmp;
            // При равенстве - по идентификатору для детерминированности
            return Long.compare(r1.getId(), r2.getId());
        });

        return rooms.stream().map(this::toResponse).toList();
    }

    /**
     * Получает номер по ID.
     */
    @Transactional(readOnly = true)
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    /**
     * Подтверждает доступность номера на запрошенные даты (временная блокировка).
     * Используется в двухшаговой согласованности при бронировании.
     */
    @Transactional
    public boolean confirmAvailability(RoomAvailabilityRequest request) {
        Optional<Room> roomOpt = roomRepository.findById(request.getRoomId());
        if (roomOpt.isEmpty()) {
            return false;
        }

        Room room = roomOpt.get();
        
        // Проверка операционной доступности номера
        if (!room.isAvailable()) {
            return false;
        }

        // Временная блокировка (в реальном проекте лучше использовать оптимистичную блокировку или пессимистичную)
        // Здесь упрощённо - просто помечаем как временно заблокированный
        room.setAvailable(false);
        
        // В реальном проекте здесь нужно проверять пересечение дат с существующими бронированиями
        // Для упрощения считаем, что если номер не заблокирован и доступен, то свободен
        
        return true;
    }

    /**
     * Снимает временную блокировку слота (компенсация при сбое).
     */
    @Transactional
    public void releaseRoom(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            // Возвращаем доступность, но не увеличиваем times_booked (это делает Booking Service)
            room.setAvailable(true);
            roomRepository.save(room);
        }
    }

    /**
     * Увеличивает счётчик бронирований для номера.
     */
    @Transactional
    public void incrementBookingCount(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.incrementTimesBooked();
            roomRepository.save(room);
        }
    }

    /**
     * Преобразует Room в AvailableRoomsResponse.
     */
    private AvailableRoomsResponse toResponse(Room room) {
        return new AvailableRoomsResponse(
                room.getId(),
                room.getNumber(),
                room.getRoomType(),
                room.getMaxOccupancy(),
                room.getTimesBooked()
        );
    }
}
