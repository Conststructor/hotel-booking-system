package com.hotel.booking.service.service;

import com.hotel.booking.service.dto.CreateBookingRequest;
import com.hotel.booking.service.dto.UserBookingsResponse;
import com.hotel.booking.service.entity.Booking;
import com.hotel.booking.service.entity.User;
import com.hotel.booking.service.feign.HotelManagementServiceClient;
import com.hotel.booking.service.repository.BookingRepository;
import com.hotel.management.service.dto.RoomAvailabilityRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Service для работы с бронированиями.
 * Реализует двухшаговую согласованность с Hotel Management Service.
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final HotelManagementServiceClient hotelClient;

//    @Lazy
//    private final UserService userService;     // ← Требует UserService

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Создает новое бронирование с двухшаговой согласованностью.
     * Шаги:
     * 1. Локальная транзакция в Booking Service - создаёт бронирование в статусе PENDING
     * 2. Вызов Hotel Management Service для подтверждения доступности номера
     * 3. При успехе - переводит бронирование в CONFIRMED и увеличивает счётчик бронирований
     * 4. При ошибке/тайм-ауте - выполняет компенсацию (CANCELLED + release слота)
     */
    @Transactional
    public Booking createBooking(CreateBookingRequest request, Long userId) {
        // Генерируем корреляционный ID запроса (если не предоставлен)
        String requestId = request.getRequestId() != null
                ? request.getRequestId()
                : UUID.randomUUID().toString();


        // Преобразуем строковые даты в LocalDateTime
        LocalDateTime startDate = parseDateTime(request.getStartDateTime());
        LocalDateTime endDate = parseDateTime(request.getEndDateTime());

        logger.info("[Booking] Starting 2-phase commit for requestId: {}", requestId);

        Booking booking = null;
        try {
            // ШАГ 1: Локальная транзакция - создаём бронирование в статусе PENDING
            booking = createPendingBooking(userId, request.getRoomId(),
                    startDate, endDate, requestId);

            logger.info("[Booking] Step 1: PENDING booking created with id: {}", booking.getId());

            // ШАГ 2: Вызов Hotel Management Service для подтверждения доступности
            boolean confirmed = confirmAvailability(booking, request, requestId);

            if (confirmed) {
                // Успех - переводим в CONFIRMED и увеличиваем счётчик бронирований
                booking.changeStatus(Booking.Status.CONFIRMED);
                hotelClient.incrementBookingCount(booking.getRoomId());

                logger.info("[Booking] Step 2: Booking CONFIRMED for requestId: {}", requestId);
            } else {
                // Ошибка подтверждения - выполняем компенсацию
                compensateFailedConfirmation(booking, request, requestId);

                logger.info("[Booking] Step 2: Confirmation failed, CANCELLED for requestId: {}", requestId);
            }

            return booking;

        } catch (Exception e) {
            // Необработанная ошибка - выполняем компенсацию
            compensateFailedConfirmation(booking, request, requestId);

            logger.error("[Booking] Step 2: Exception during confirmation, CANCELLED for requestId: {}",
                    requestId, e);

            throw e;
        }
    }

    /**
     * Создает бронирование в статусе PENDING.
     */
    private Booking createPendingBooking(Long userId, Long roomId, LocalDateTime startDate, 
                                          LocalDateTime endDate, String requestId) {
        // Проверяем идемпотентность запроса
        Optional<Booking> existing = bookingRepository.findByIdAndUserId(Long.valueOf(requestId), userId);
        if (existing.isPresent()) {
            logger.info("[Booking] Idempotent request detected for requestId: {}", requestId);
            return existing.get();
        }

        // Проверяем конфликт бронирований
        if (bookingRepository.hasConflict(roomId, startDate, endDate)) {
            throw new RuntimeException("Room already booked for the requested period");
        }

        Booking booking = new Booking(userId, roomId, startDate, endDate, requestId);
        return bookingRepository.save(booking);
    }

    /**
     * Подтверждает доступность номера через Hotel Management Service.
     */
    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public boolean confirmAvailability(Booking booking, CreateBookingRequest request, String requestId) {
        logger.info("[Booking] Step 2a: Confirming availability for roomId: {}, requestId: {}", 
                booking.getRoomId(), requestId);

        try {
            RoomAvailabilityRequest availabilityRequest = new RoomAvailabilityRequest(
                    booking.getRoomId(),
                    startDateToString(booking.getStartDate()),
                    startDateToString(booking.getEndDate())
            );

            boolean available = hotelClient.confirmAvailability(booking.getRoomId(), availabilityRequest);

            if (available) {
                logger.info("[Booking] Step 2b: Availability confirmed for roomId: {}", booking.getRoomId());
                return true;
            } else {
                logger.warn("[Booking] Step 2b: Availability not confirmed for roomId: {}", booking.getRoomId());
                return false;
            }

        } catch (Exception e) {
            logger.error("[Booking] Step 2c: Exception during availability check for roomId: {}", 
                    booking.getRoomId(), e);
            throw new RuntimeException("Failed to confirm availability", e);
        }
    }

    /**
     * Выполняет компенсацию при сбое подтверждения.
     */
    private void compensateFailedConfirmation(Booking booking, CreateBookingRequest request, String requestId) {
        logger.info("[Booking] Step 2d: Compensating failed confirmation for requestId: {}", requestId);

        // Меняем статус на CANCELLED в локальной транзакции
        booking.changeStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);

        // Снимаем временную блокировку в Hotel Management Service
        try {
            hotelClient.releaseRoom(booking.getRoomId());
            logger.info("[Booking] Step 2e: Release called for roomId: {}", booking.getRoomId());
        } catch (Exception e) {
            logger.warn("[Booking] Step 2f: Exception during release, but already CANCELLED", e);
        }

        // При повторной доставке проверяем requestId для идемпотентности
        checkAndHandleDuplicateRequest(request, booking, requestId);
    }

    /**
     * Проверяет и обрабатывает дублирующиеся запросы.
     */
    private void checkAndHandleDuplicateRequest(CreateBookingRequest request, Booking booking, String requestId) {
        // Если requestId совпадает и статус CANCELLED - это повторная попытка
        if (request.getRequestId() != null && 
            request.getRequestId().equals(booking.getRequestId()) && 
            booking.getStatus() == Booking.Status.CANCELLED) {
            
            logger.info("[Booking] Duplicate request detected, reprocessing for requestId: {}", requestId);
            // Повторная попытка создания бронирования (идемпотентность через requestId)
            try {
                createPendingBooking(booking.getUserId(), booking.getRoomId(), 
                        booking.getStartDate(), booking.getEndDate(), requestId);
                
                boolean confirmed = confirmAvailability(booking, request, requestId);
                
                if (confirmed) {
                    booking.changeStatus(Booking.Status.CONFIRMED);
                    hotelClient.incrementBookingCount(booking.getRoomId());
                    logger.info("[Booking] Duplicate request successfully reprocessed");
                } else {
                    compensateFailedConfirmation(booking, request, requestId);
                    logger.info("[Booking] Duplicate request failed after reprocessing");
                }
            } catch (Exception e) {
                logger.error("[Booking] Exception during duplicate request processing", e);
                compensateFailedConfirmation(booking, request, requestId);
            }
        }
    }

    /**
     * Получает все бронирования пользователя.
     */
    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Получает бронирование по ID (с проверкой права доступа).
     */
    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Long id, Long userId) {
        return bookingRepository.findByIdAndUserId(id, userId);
    }

    /**
     * Отменяет бронирование.
     */
    @Transactional
    public void cancelBooking(Long id, Long userId) {
        Optional<Booking> bookingOpt = bookingRepository.findByIdAndUserId(id, userId);
        
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        if (booking.getStatus() == Booking.Status.CONFIRMED) {
            // Для CONFIRMED бронирования нужно вызвать release в Hotel Service
            try {
                hotelClient.releaseRoom(booking.getRoomId());
            } catch (Exception e) {
                logger.warn("Exception during release for confirmed booking: {}", id, e);
            }
        }

        booking.changeStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);
    }

    /**
     * Преобразует LocalDateTime в строку.
     */
    private String startDateToString(LocalDateTime date) {
        return date.format(DATE_TIME_FORMATTER);
    }

    /**
     * Преобразует строку в LocalDateTime.
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + dateTimeStr, e);
        }
    }

    /**
     * Преобразует список бронирований в пользовательский формат ответа.
     */
    @Transactional(readOnly = true)
    public UserBookingsResponse getBookingDetails(Booking booking) {
       return new UserBookingsResponse(
                            booking.getId(),
                            booking.getRoomId(),
                            booking.getStartDate(),
                            booking.getEndDate(),
                            booking.getStatus().name(),
                            booking.getCreatedAt()
                    );
    }
}
