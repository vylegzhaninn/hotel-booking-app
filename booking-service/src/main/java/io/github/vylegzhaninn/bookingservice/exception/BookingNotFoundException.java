package io.github.vylegzhaninn.bookingservice.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(long bookingId) {
        super("Бронирование не найдено: " + bookingId);
    }
}
