package com.hotel.management.service.repository;

import com.hotel.management.service.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository для работы с отелями.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
