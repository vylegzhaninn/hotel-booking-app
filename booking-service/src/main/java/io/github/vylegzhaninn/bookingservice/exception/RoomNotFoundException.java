package io.github.vylegzhaninn.bookingservice.exception;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(long roomId) {
        super("Номер не найден: " + roomId);
    }
}
