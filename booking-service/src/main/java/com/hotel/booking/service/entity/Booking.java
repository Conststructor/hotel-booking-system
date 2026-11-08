package com.hotel.booking.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность Бронирование - информация о бронировании номера.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long roomId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "request_id", length = 50)
    private String requestId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Booking(Long userId, Long roomId, LocalDateTime startDate, 
                   LocalDateTime endDate, String requestId) {
        this.userId = userId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestId = requestId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Перечисление статусов бронирования.
     */
    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    /**
     * Меняет статус бронирования.
     */
    public void changeStatus(Status newStatus) {
        this.status = newStatus;
    }
}
