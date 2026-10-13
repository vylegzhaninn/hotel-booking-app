package io.github.vylegzhaninn.paymentservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Событие бронирования из топика {@code booking-events}, публикуемого booking-сервисом.
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
