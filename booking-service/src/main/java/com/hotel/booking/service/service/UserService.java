package com.hotel.booking.service.service;

import com.hotel.booking.service.dto.AuthRequest;
import com.hotel.booking.service.dto.RegisterRequest;
import com.hotel.booking.service.entity.User;
import com.hotel.booking.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Service для работы с пользователями (регистрация, авторизация).
 */
@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Регистрирует нового пользователя.
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User already exists: " + request.getUsername());
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                User.Role.USER
        );

        return userRepository.save(user);
    }

    /**
     * Авторизует пользователя и возвращает JWT токен.
     */
    @Transactional(readOnly = true)
    public String authenticate(AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + request.getUsername());
        }

        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // В реальном проекте здесь нужно создать токен с использованием JwtTokenProvider
        // Для упрощения возвращаем username + mock-токен
        return "Bearer " + user.getUsername() + "_TOKEN_" + System.currentTimeMillis();
    }

    /**
     * Получает пользователя по ID.
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Находит пользователя по username.
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Обновляет данные пользователя (ADMIN).
     */
    @Transactional
    public User updateUser(Long id, String username, String password, User.Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (username != null) {
            user.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (role != null) {
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя (ADMIN).
     */
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
