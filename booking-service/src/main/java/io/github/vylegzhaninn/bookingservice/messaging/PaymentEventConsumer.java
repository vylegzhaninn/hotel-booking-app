package io.github.vylegzhaninn.bookingservice.messaging;

import tools.jackson.databind.ObjectMapper;
import io.github.vylegzhaninn.bookingservice.config.KafkaTopicsConfig;
import io.github.vylegzhaninn.bookingservice.model.PaymentEvent;
import io.github.vylegzhaninn.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicsConfig.PAYMENT_EVENTS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentEvent(String payload) {
        try {
            var event = objectMapper.readValue(payload, PaymentEvent.class);
            switch (event.type()) {
                case PAYMENT_SUCCEEDED -> bookingService.confirmPayment(event.bookingId());
                case PAYMENT_FAILED -> bookingService.rejectPayment(event.bookingId());
                case PAYMENT_REFUNDED -> log.debug("Платёж по бронированию {} возвращён", event.bookingId());
            }
        } catch (Exception e) {
            log.warn("Не удалось обработать событие платежа: {}", payload, e);
        }
    }
}
