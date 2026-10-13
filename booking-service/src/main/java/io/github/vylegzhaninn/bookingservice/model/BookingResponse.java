package io.github.vylegzhaninn.bookingservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Бронирование")
public record BookingResponse(
    @Schema(description = "Идентификатор бронирования", example = "1")
    Long id,
    @Schema(description = "Идентификатор номера", example = "1")
    Long roomId,
    @Schema(description = "Название отеля", example = "Grand Palace")
    String hotelName,
    @Schema(description = "Номер комнаты", example = "101")
    String roomNumber,
    @Schema(description = "Дата заезда", example = "2026-11-01")
    LocalDate checkIn,
    @Schema(description = "Дата выезда", example = "2026-11-05")
    LocalDate checkOut,
    @Schema(description = "Статус бронирования", example = "PENDING")
    BookingStatus status,
    @Schema(description = "Итоговая сумма, руб.", example = "18000.00")
    BigDecimal totalAmount,
    @Schema(description = "Момент, до которого номер удерживается за пользователем")
    Instant expiresAt
) {
}
