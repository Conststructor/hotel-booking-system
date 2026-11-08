package com.hotel.management.service.service;

import com.hotel.management.service.dto.CreateHotelRequest;
import com.hotel.management.service.entity.Hotel;
import com.hotel.management.service.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service для работы с отелями.
 */
@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * Создает новый отель.
     */
    @Transactional
    public Hotel createHotel(CreateHotelRequest request) {
        Hotel hotel = new Hotel(
                request.getName(),
                request.getAddress(),
                request.getDescription()
        );
        return hotelRepository.save(hotel);
    }

    /**
     * Получает список всех отелей.
     */
    @Transactional(readOnly = true)
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    /**
     * Получает отель по ID.
     */
    @Transactional(readOnly = true)
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    /**
     * Обновляет данные отеля.
     */
    @Transactional
    public Hotel updateHotel(Long id, CreateHotelRequest request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + id));
        
        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setDescription(request.getDescription());
        
        return hotelRepository.save(hotel);
    }

    /**
     * Удаляет отель по ID.
     */
    @Transactional
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}
