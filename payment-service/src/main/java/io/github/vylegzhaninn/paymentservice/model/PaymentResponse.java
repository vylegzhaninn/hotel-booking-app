package io.github.vylegzhaninn.paymentservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Платёж по бронированию")
public record PaymentResponse(
    @Schema(description = "Идентификатор платежа", example = "1")
    Long id,
    @Schema(description = "Идентификатор бронирования", example = "1")
    Long bookingId,
    @Schema(description = "Сумма, руб.", example = "18000.00")
    BigDecimal amount,
    @Schema(description = "Статус платежа", example = "SUCCEEDED")
    PaymentStatus status,
    @Schema(description = "Время создания")
    Instant createdAt
) {
}
