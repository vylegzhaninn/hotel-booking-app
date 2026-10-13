package io.github.vylegzhaninn.bookingservice.exception;

public class RoomUnavailableException extends RuntimeException {

    public RoomUnavailableException(long roomId) {
        super("Номер на выбранные даты уже занят: " + roomId);
    }
}
