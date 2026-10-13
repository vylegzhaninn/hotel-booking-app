package io.github.vylegzhaninn.bookingservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Запрос на создание бронирования")
public record CreateBookingRequest(
    @Schema(description = "Идентификатор номера", example = "1")
    @NotNull
    Long roomId,
    @Schema(description = "Дата заезда", example = "2026-11-01")
    @NotNull
    LocalDate checkIn,
    @Schema(description = "Дата выезда (не включая день выезда)", example = "2026-11-05")
    @NotNull
    LocalDate checkOut
) {
}
