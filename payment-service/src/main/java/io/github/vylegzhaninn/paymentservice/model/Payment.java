package io.github.vylegzhaninn.paymentservice.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Payment(
    Long id,
    Long bookingId,
    String userName,
    BigDecimal amount,
    PaymentStatus status,
    Instant createdAt
) {
}
