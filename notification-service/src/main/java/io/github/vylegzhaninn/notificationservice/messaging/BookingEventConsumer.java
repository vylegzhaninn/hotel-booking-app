package io.github.vylegzhaninn.notificationservice.messaging;

import tools.jackson.databind.ObjectMapper;
import io.github.vylegzhaninn.notificationservice.config.KafkaTopicsConfig;
import io.github.vylegzhaninn.notificationservice.model.BookingEvent;
import io.github.vylegzhaninn.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicsConfig.BOOKING_EVENTS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingEvent(String payload) {
        try {
            notificationService.onBookingEvent(objectMapper.readValue(payload, BookingEvent.class));
        } catch (Exception e) {
            log.warn("Не удалось обработать событие бронирования: {}", payload, e);
        }
    }
}
