package io.github.vylegzhaninn.bookingservice.messaging;

import tools.jackson.databind.ObjectMapper;
import io.github.vylegzhaninn.bookingservice.config.KafkaTopicsConfig;
import io.github.vylegzhaninn.bookingservice.exception.EventPublishException;
import io.github.vylegzhaninn.bookingservice.model.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(BookingEvent event) {
        try {
            kafkaTemplate
                .send(KafkaTopicsConfig.BOOKING_EVENTS_TOPIC, String.valueOf(event.bookingId()), toJson(event))
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Опубликовано событие {} по бронированию {}", event.type(), event.bookingId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException(KafkaTopicsConfig.BOOKING_EVENTS_TOPIC, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new EventPublishException(KafkaTopicsConfig.BOOKING_EVENTS_TOPIC, e);
        }
    }

    private String toJson(BookingEvent event) {
        return objectMapper.writeValueAsString(event);
    }
}
