package io.github.vylegzhaninn.bookingservice.model;

import java.math.BigDecimal;

public record Room(
    Long id,
    String hotelName,
    String roomNumber,
    RoomCategory category,
    BigDecimal pricePerNight
) {
}
