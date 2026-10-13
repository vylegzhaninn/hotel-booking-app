package io.github.vylegzhaninn.bookingservice.exception;

public class EventPublishException extends RuntimeException {

    public EventPublishException(String topic, Throwable cause) {
        super("Не удалось опубликовать событие в топик " + topic, cause);
    }
}
