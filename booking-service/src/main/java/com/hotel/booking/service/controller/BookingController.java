package com.hotel.booking.service.controller;

import com.hotel.booking.service.dto.AuthRequest;
import com.hotel.booking.service.dto.CreateBookingRequest;
import com.hotel.booking.service.dto.RegisterRequest;
import com.hotel.booking.service.dto.UserBookingsResponse;
import com.hotel.booking.service.entity.Booking;
import com.hotel.booking.service.entity.User;
import com.hotel.booking.service.security.JwtTokenProvider;
import com.hotel.booking.service.service.BookingService;
import com.hotel.booking.service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * REST контроллер для управления пользователями и бронированиями.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
//    private final BookingDetailService bookingDetailService;

    /**
     * Регистрирует нового пользователя.
     */
    @PostMapping("/user/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.ok(user);
    }

    /**
     * Авторизует пользователя и возвращает JWT токен.
     */
    @PostMapping("/user/auth")
    public ResponseEntity<String> authenticate(@Valid @RequestBody AuthRequest request) {
        String token = userService.authenticate(request);
        return ResponseEntity.ok(token);
    }

    /**
     * Создает новое бронирование (USER, ADMIN).
     */
    @PostMapping("/bookings")
    public ResponseEntity<com.hotel.booking.service.entity.Booking> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        
        // Получаем текущего пользователя из токена
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        com.hotel.booking.service.entity.Booking booking = bookingService.createBooking(request, user.getId());
        return ResponseEntity.ok(booking);
    }

    /**
     * Получает историю бронирований пользователя (USER, ADMIN).
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<UserBookingsResponse>> getUserBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<com.hotel.booking.service.entity.Booking> bookings = bookingService.getUserBookings(user.getId());
        List<UserBookingsResponse> responses = Collections.singletonList(bookingService.getBookingDetails((Booking) bookings));
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Получает бронирование по ID (USER, ADMIN).
     */
    @GetMapping("/booking/{id}")
    public ResponseEntity<com.hotel.booking.service.entity.Booking> getBookingById(
            @PathVariable Long id) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return bookingService.getBookingById(id, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Отменяет бронирование (USER, ADMIN).
     */
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        bookingService.cancelBooking(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновляет данные пользователя (ADMIN).
     */
    @PatchMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Получаем ID текущего пользователя из токена (в упрощённой версии - по username)
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), 
                user.getUsername(), user.getPassword(), user.getRole()));
    }

    /**
     * Удаляет пользователя (ADMIN).
     */
    @DeleteMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
