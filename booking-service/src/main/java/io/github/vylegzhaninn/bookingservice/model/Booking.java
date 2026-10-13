package io.github.vylegzhaninn.bookingservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record Booking(
    Long id,
    Long roomId,
    String userName,
    LocalDate checkIn,
    LocalDate checkOut,
    BookingStatus status,
    BigDecimal totalAmount,
    Instant expiresAt,
    Instant createdAt
) {
}
