package io.github.vylegzhaninn.bookingservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Свободный номер на выбранные даты")
public record RoomResponse(
    @Schema(description = "Идентификатор номера", example = "1")
    Long id,
    @Schema(description = "Название отеля", example = "Grand Palace")
    String hotelName,
    @Schema(description = "Номер комнаты", example = "101")
    String roomNumber,
    @Schema(description = "Категория", example = "STANDARD")
    RoomCategory category,
    @Schema(description = "Цена за ночь, руб.", example = "4500.00")
    BigDecimal pricePerNight
) {
}
