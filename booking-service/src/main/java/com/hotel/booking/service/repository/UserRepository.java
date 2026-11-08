package com.hotel.booking.service.repository;

import com.hotel.booking.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository для работы с пользователями.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет существование пользователя по username.
     */
    boolean existsByUsername(String username);
}
