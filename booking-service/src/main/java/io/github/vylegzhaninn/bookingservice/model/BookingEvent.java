package io.github.vylegzhaninn.bookingservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Событие жизненного цикла бронирования, публикуемое в топик {@code booking-events}.
 */
public record BookingEvent(
    BookingEventType type,
    Long bookingId,
    Long roomId,
    String hotelName,
    String roomNumber,
    String userName,
    LocalDate checkIn,
    LocalDate checkOut,
    BigDecimal totalAmount,
    Instant occurredAt
) {
}
