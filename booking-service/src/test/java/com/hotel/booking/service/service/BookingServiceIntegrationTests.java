package com.hotel.booking.service.service;

import com.hotel.booking.service.dto.CreateBookingRequest;
import com.hotel.booking.service.entity.Booking;
import com.hotel.booking.service.entity.User;
import com.hotel.booking.service.repository.BookingRepository;
import com.hotel.booking.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты для Booking Service.
 */
@SpringBootTest
@ActiveProfiles("test")
public class BookingServiceIntegrationTests {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        // Создаём тестового пользователя
        User user = new User("testuser", "password123", User.Role.USER);
        testUser = userRepository.save(user);
    }

    @Test
    public void testCreateBooking_ShouldCreatePendingBooking() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                1L,  // roomId
                "2024-12-01T10:00:00Z",
                "2024-12-05T10:00:00Z",
                false,
                "test-request-id-1"
        );

        // Act
        Booking booking = bookingService.createBooking(request, testUser.getId());

        // Assert
        assertThat(booking).isNotNull();
        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getRoomId()).isEqualTo(1L);
        assertThat(booking.getUserId()).isEqualTo(testUser.getId());
        assertThat(booking.getStatus()).isEqualTo(Booking.Status.PENDING);
        assertThat(booking.getRequestId()).isEqualTo("test-request-id-1");
    }

    @Test
    public void testCreateBooking_ShouldHandleIdempotentRequest() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "2024-12-01T10:00:00Z",
                "2024-12-05T10:00:00Z",
                false,
                "test-request-id-2"
        );

        // Act - первый раз
        Booking booking1 = bookingService.createBooking(request, testUser.getId());
        
        // Act - второй раз с тем же requestId (идемпотентность)
        Booking booking2 = bookingService.createBooking(request, testUser.getId());

        // Assert - должны вернуться один и тот же объект
        assertThat(booking1).isSameAs(booking2);
    }

    @Test
    public void testGetUserBookings_ShouldReturnAllBookings() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "2024-12-01T10:00:00Z",
                "2024-12-05T10:00:00Z",
                false,
                "test-request-id-3"
        );

        bookingService.createBooking(request, testUser.getId());

        // Act
        List<Booking> bookings = bookingService.getUserBookings(testUser.getId());

        // Assert
        assertThat(bookings).hasSize(1);
    }

    @Test
    public void testGetBookingById_ShouldReturnBooking() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "2024-12-01T10:00:00Z",
                "2024-12-05T10:00:00Z",
                false,
                "test-request-id-4"
        );

        Booking booking = bookingService.createBooking(request, testUser.getId());

        // Act
        Optional<Booking> found = bookingService.getBookingById(booking.getId(), testUser.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(booking.getId());
    }

    @Test
    public void testGetBookingById_WithWrongUser_ShouldReturnNotFound() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "2024-12-01T10:00:00Z",
                "2024-12-05T10:00:00Z",
                false,
                "test-request-id-5"
        );

        Booking booking = bookingService.createBooking(request, testUser.getId());

        // Act - пробуем получить с другим user_id
        Optional<Booking> found = bookingService.getBookingById(booking.getId(), 999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    public void testCancelBooking_ShouldChangeStatusToCancelled() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "2024-12-01T10:00:00Z",
                "2024-12-05T10:00:00Z",
                false,
                "test-request-id-6"
        );

        Booking booking = bookingService.createBooking(request, testUser.getId());
        
        // Убеждаемся что статус PENDING
        assertThat(booking.getStatus()).isEqualTo(Booking.Status.PENDING);

        // Act
        bookingService.cancelBooking(booking.getId(), testUser.getId());

        // Assert
        Optional<Booking> found = bookingRepository.findById(booking.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(Booking.Status.CANCELLED);
    }

    @Test
    public void testCancelNonExistentBooking_ShouldThrowException() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                "2024-12-01T10:00:00Z",
                "2024-12-05T10:00:00Z",
                false,
                "test-request-id-7"
        );

        bookingService.createBooking(request, testUser.getId());

        // Act & Assert
        assertThatThrownBy(() -> 
            bookingService.cancelBooking(999L, testUser.getId())
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Booking not found");
    }

    @Test
    public void testCreateMultipleBookings_ShouldHaveDifferentIds() {
        // Arrange
        CreateBookingRequest request1 = new CreateBookingRequest(
                1L, "2024-12-01T10:00:00Z", "2024-12-05T10:00:00Z", false, "unique-id-1"
        );
        
        CreateBookingRequest request2 = new CreateBookingRequest(
                2L, "2024-12-06T10:00:00Z", "2024-12-10T10:00:00Z", false, "unique-id-2"
        );

        // Act
        Booking booking1 = bookingService.createBooking(request1, testUser.getId());
        Booking booking2 = bookingService.createBooking(request2, testUser.getId());

        // Assert
        assertThat(booking1.getId()).isNotNull();
        assertThat(booking2.getId()).isNotNull();
        assertThat(booking1.getId()).isNotEqualTo(booking2.getId());
    }
}
