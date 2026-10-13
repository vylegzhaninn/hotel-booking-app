package io.github.vylegzhaninn.bookingservice.exception;

public class BookingAccessException extends RuntimeException {

    public BookingAccessException(long bookingId, String userName) {
        super("Бронирование %d не принадлежит пользователю %s".formatted(bookingId, userName));
    }
}
