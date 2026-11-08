package com.hotel.management.service.repository;

import com.hotel.management.service.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository для работы с номерами.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Получает список номеров по hotelId
     */
    List<Room> findByHotelId(Long hotelId);

    /**
     * Получает список доступных номеров
     */
    List<Room> findByAvailableTrue();

    /**
     * Получает количество бронирований для номера (для алгоритма равномерной загрузки).
     */
    @Query("SELECT r.timesBooked FROM Room r WHERE r.id = :roomId")
    Integer getTimesBooked(Long roomId);

    /**
     * Увеличивает счётчик бронирований для номера.
     */
    @Query("UPDATE Room r SET r.timesBooked = r.timesBooked + 1 WHERE r.id = :roomId")
    void incrementTimesBooked(Long roomId);
}
