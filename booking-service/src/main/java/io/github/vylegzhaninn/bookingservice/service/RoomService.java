package io.github.vylegzhaninn.bookingservice.service;

import io.github.vylegzhaninn.bookingservice.exception.InvalidBookingDatesException;
import io.github.vylegzhaninn.bookingservice.mapper.RoomMapper;
import io.github.vylegzhaninn.bookingservice.model.RoomResponse;
import io.github.vylegzhaninn.bookingservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public List<RoomResponse> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null && checkOut == null) {
            return roomMapper.toResponseList(roomRepository.findAll());
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new InvalidBookingDatesException(
                "Укажите даты заезда и выезда, причём выезд должен быть позже заезда");
        }
        return roomMapper.toResponseList(roomRepository.findAvailable(checkIn, checkOut));
    }
}
