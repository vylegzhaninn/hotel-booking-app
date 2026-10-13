package io.github.vylegzhaninn.bookingservice.exception;

public class InvalidBookingDatesException extends RuntimeException {

    public InvalidBookingDatesException(String message) {
        super(message);
    }
}
