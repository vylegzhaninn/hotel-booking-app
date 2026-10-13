package io.github.vylegzhaninn.bookingservice.controller;

import io.github.vylegzhaninn.bookingservice.model.BookingResponse;
import io.github.vylegzhaninn.bookingservice.model.CreateBookingRequest;
import io.github.vylegzhaninn.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Bookings", description = "Бронирование номеров")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    static final String USER_NAME_HEADER = "X-User-Name";

    private final BookingService bookingService;

    @Operation(summary = "Создать бронирование",
        description = "Удерживает номер за пользователем на время холда и запускает процесс оплаты")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Бронирование создано, ожидает оплаты"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса"),
        @ApiResponse(responseCode = "404", description = "Номер не найден"),
        @ApiResponse(responseCode = "409", description = "Номер на выбранные даты уже занят")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
        @RequestHeader(USER_NAME_HEADER) String userName,
        @Valid @RequestBody CreateBookingRequest request
    ) {
        return bookingService.create(userName, request);
    }

    @Operation(summary = "Мои бронирования", description = "Возвращает бронирования текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список бронирований")
    @GetMapping
    public List<BookingResponse> findUserBookings(@RequestHeader(USER_NAME_HEADER) String userName) {
        return bookingService.findUserBookings(userName);
    }

    @Operation(summary = "Отменить бронирование", description = "Отменяет бронирование и возвращает средства при успешной оплате")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Бронирование отменено"),
        @ApiResponse(responseCode = "403", description = "Бронирование принадлежит другому пользователю"),
        @ApiResponse(responseCode = "404", description = "Бронирование не найдено"),
        @ApiResponse(responseCode = "409", description = "Бронирование уже завершено")
    })
    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(
        @RequestHeader(USER_NAME_HEADER) String userName,
        @PathVariable long id
    ) {
        return bookingService.cancel(userName, id);
    }
}
