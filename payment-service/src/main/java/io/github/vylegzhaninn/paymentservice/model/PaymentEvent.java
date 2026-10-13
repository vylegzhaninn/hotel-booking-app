package io.github.vylegzhaninn.paymentservice.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Событие платежа, публикуемое в топик {@code payment-events}.
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
