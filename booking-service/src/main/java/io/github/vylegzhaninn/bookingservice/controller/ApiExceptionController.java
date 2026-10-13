package io.github.vylegzhaninn.bookingservice.controller;

import io.github.vylegzhaninn.bookingservice.exception.BookingAccessException;
import io.github.vylegzhaninn.bookingservice.exception.BookingNotFoundException;
import io.github.vylegzhaninn.bookingservice.exception.BookingStateException;
import io.github.vylegzhaninn.bookingservice.exception.InvalidBookingDatesException;
import io.github.vylegzhaninn.bookingservice.exception.RoomNotFoundException;
import io.github.vylegzhaninn.bookingservice.exception.RoomUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionController {

    @ExceptionHandler({RoomNotFoundException.class, BookingNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RoomUnavailableException.class)
    public ProblemDetail handleRoomUnavailable(RoomUnavailableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BookingStateException.class)
    public ProblemDetail handleBookingState(BookingStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BookingAccessException.class)
    public ProblemDetail handleBookingAccess(BookingAccessException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({InvalidBookingDatesException.class, MissingRequestHeaderException.class})
    public ProblemDetail handleBadRequest(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
