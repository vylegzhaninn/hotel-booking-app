package io.github.vylegzhaninn.bookingservice.controller;

import io.github.vylegzhaninn.bookingservice.model.RoomResponse;
import io.github.vylegzhaninn.bookingservice.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Rooms", description = "Поиск свободных номеров")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "Список номеров", description = "Возвращает все номера либо свободные на выбранные даты")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список номеров"),
        @ApiResponse(responseCode = "400", description = "Некорректные даты поиска")
    })
    @GetMapping
    public List<RoomResponse> findAvailable(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        return roomService.findAvailable(checkIn, checkOut);
    }
}
