package io.github.vylegzhaninn.paymentservice.messaging;

import tools.jackson.databind.ObjectMapper;
import io.github.vylegzhaninn.paymentservice.config.KafkaTopicsConfig;
import io.github.vylegzhaninn.paymentservice.model.PaymentEvent;
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
public class PaymentEventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(PaymentEvent event) {
        try {
            kafkaTemplate
                .send(KafkaTopicsConfig.PAYMENT_EVENTS_TOPIC, String.valueOf(event.bookingId()), toJson(event))
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Опубликовано событие {} по бронированию {}", event.type(), event.bookingId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Прервана публикация события платежа", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Не удалось опубликовать событие платежа", e);
        }
    }

    private String toJson(PaymentEvent event) {
        return objectMapper.writeValueAsString(event);
    }
}
