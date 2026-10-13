package io.github.vylegzhaninn.bookingservice.service;

import io.github.vylegzhaninn.bookingservice.config.BookingProperties;
import io.github.vylegzhaninn.bookingservice.exception.BookingAccessException;
import io.github.vylegzhaninn.bookingservice.exception.BookingNotFoundException;
import io.github.vylegzhaninn.bookingservice.exception.BookingStateException;
import io.github.vylegzhaninn.bookingservice.exception.InvalidBookingDatesException;
import io.github.vylegzhaninn.bookingservice.exception.RoomNotFoundException;
import io.github.vylegzhaninn.bookingservice.exception.RoomUnavailableException;
import io.github.vylegzhaninn.bookingservice.messaging.BookingEventPublisher;
import io.github.vylegzhaninn.bookingservice.model.Booking;
import io.github.vylegzhaninn.bookingservice.model.BookingEvent;
import io.github.vylegzhaninn.bookingservice.model.BookingEventType;
import io.github.vylegzhaninn.bookingservice.model.BookingResponse;
import io.github.vylegzhaninn.bookingservice.model.BookingStatus;
import io.github.vylegzhaninn.bookingservice.model.CreateBookingRequest;
import io.github.vylegzhaninn.bookingservice.model.Room;
import io.github.vylegzhaninn.bookingservice.repository.BookingRepository;
import io.github.vylegzhaninn.bookingservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final String NO_OVERLAP_CONSTRAINT = "excl_bookings_no_overlap";

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final HoldService holdService;
    private final BookingEventPublisher eventPublisher;
    private final BookingProperties properties;

    public BookingResponse create(String userName, CreateBookingRequest request) {
        validateDates(request);
        Room room = roomRepository.findById(request.roomId())
            .orElseThrow(() -> new RoomNotFoundException(request.roomId()));
        if (bookingRepository.existsActiveOverlap(room.id(), request.checkIn(), request.checkOut())) {
            throw new RoomUnavailableException(room.id());
        }
        Booking booking = insertBooking(userName, request, room);
        holdService.acquire(booking.id(), room.id(), properties.holdTtl());
        eventPublisher.publish(toEvent(BookingEventType.BOOKING_CREATED, booking, room));
        return bookingRepository.findResponseById(booking.id()).orElseThrow();
    }

    public List<BookingResponse> findUserBookings(String userName) {
        return bookingRepository.findResponsesByUserName(userName);
    }

    public BookingResponse cancel(String userName, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (!booking.userName().equals(userName)) {
            throw new BookingAccessException(bookingId, userName);
        }
        if (booking.status() != BookingStatus.PENDING && booking.status() != BookingStatus.CONFIRMED) {
            throw new BookingStateException(bookingId, booking.status());
        }
        if (bookingRepository.updateStatusIfCurrent(bookingId, booking.status(), BookingStatus.CANCELLED) == 0) {
            throw new BookingStateException(bookingId, booking.status());
        }
        holdService.release(bookingId);
        eventPublisher.publish(toEvent(BookingEventType.BOOKING_CANCELLED, booking, roomOf(booking)));
        return bookingRepository.findResponseById(bookingId).orElseThrow();
    }

    /**
     * Подтверждает бронирование после успешного платежа. Просроченные (EXPIRED)
     * бронирования не подтверждаются — платёж по ним остаётся на совести payment-сервиса.
     */
    public void confirmPayment(long bookingId) {
        if (bookingRepository.updateStatusIfCurrent(bookingId, BookingStatus.PENDING, BookingStatus.CONFIRMED) == 0) {
            log.warn("Платёж по бронированию {} не подтверждён: оно уже не в статусе PENDING", bookingId);
            return;
        }
        holdService.release(bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        eventPublisher.publish(toEvent(BookingEventType.BOOKING_CONFIRMED, booking, roomOf(booking)));
    }

    public void rejectPayment(long bookingId) {
        if (bookingRepository.updateStatusIfCurrent(bookingId, BookingStatus.PENDING, BookingStatus.CANCELLED) == 0) {
            log.warn("Отказ платежа по бронированию {} не применён: оно уже не в статусе PENDING", bookingId);
            return;
        }
        holdService.release(bookingId);
    }

    public int expireOverduePendingBookings() {
        var expiredIds = bookingRepository.findPendingExpiredIds();
        if (expiredIds.isEmpty()) {
            return 0;
        }
        var expired = bookingRepository.markExpired(expiredIds);
        expiredIds.forEach(holdService::release);
        log.info("Просрочено и снято с холда бронирований: {}", expired);
        return expired;
    }

    private void validateDates(CreateBookingRequest request) {
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new InvalidBookingDatesException("Дата выезда должна быть позже даты заезда");
        }
        if (request.checkIn().isBefore(LocalDate.now())) {
            throw new InvalidBookingDatesException("Дата заезда не может быть в прошлом");
        }
    }

    /**
     * Гонку двух одновременных бронирований закрывает exclusion-ограничение
     * excl_bookings_no_overlap в PostgreSQL, поэтому кроме предварительной
     * проверки конфликт транслируем в 409.
     */
    private Booking insertBooking(String userName, CreateBookingRequest request, Room room) {
        try {
            return bookingRepository.insert(
                room.id(),
                userName,
                request.checkIn(),
                request.checkOut(),
                totalPrice(room, request),
                Instant.now().plus(properties.holdTtl())
            );
        } catch (UnableToExecuteStatementException e) {
            if (isOverlapViolation(e)) {
                throw new RoomUnavailableException(room.id());
            }
            throw e;
        }
    }

    private boolean isOverlapViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains(NO_OVERLAP_CONSTRAINT)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal totalPrice(Room room, CreateBookingRequest request) {
        var nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        return room.pricePerNight().multiply(BigDecimal.valueOf(nights)).setScale(2);
    }

    private BookingEvent toEvent(BookingEventType type, Booking booking, Room room) {
        return new BookingEvent(
            type,
            booking.id(),
            room.id(),
            room.hotelName(),
            room.roomNumber(),
            booking.userName(),
            booking.checkIn(),
            booking.checkOut(),
            booking.totalAmount(),
            Instant.now()
        );
    }

    private Room roomOf(Booking booking) {
        return roomRepository.findById(booking.roomId())
            .orElseThrow(() -> new RoomNotFoundException(booking.roomId()));
    }
}
