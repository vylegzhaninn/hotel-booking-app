package io.github.vylegzhaninn.bookingservice.exception;

import io.github.vylegzhaninn.bookingservice.model.BookingStatus;

public class BookingStateException extends RuntimeException {

    public BookingStateException(long bookingId, BookingStatus status) {
        super("Бронирование %d нельзя отменить в статусе %s".formatted(bookingId, status));
    }
}
