package io.github.vylegzhaninn.notificationservice.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Событие платежа из топика {@code payment-events}, публикуемого payment-сервисом.
 */
public record PaymentEvent(
    PaymentEventType type,
    Long paymentId,
    Long bookingId,
    String userName,
    BigDecimal amount,
    Instant occurredAt
) {
}
