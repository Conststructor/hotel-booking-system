package com.hotel.booking.service.repository;

import com.hotel.booking.service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с бронированиями.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Получает все бронирования пользователя.
     */
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Получает бронирование по ID.
     */
    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    /**
     * Проверяет, существует ли активное бронирование для номера в указанный период.
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.roomId = :roomId " +
           "AND (:startDate < b.endDate AND :endDate > b.startDate) " +
           "AND b.status IN ('CONFIRMED', 'PENDING')")
    boolean hasConflict(Long roomId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Получает все бронирования по ID (для проверки состояния).
     */
    Optional<Booking> findById(Long id);
}
