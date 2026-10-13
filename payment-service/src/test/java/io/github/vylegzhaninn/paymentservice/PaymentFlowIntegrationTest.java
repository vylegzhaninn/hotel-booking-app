package io.github.vylegzhaninn.paymentservice;

import tools.jackson.databind.ObjectMapper;
import io.github.vylegzhaninn.paymentservice.model.BookingEvent;
import io.github.vylegzhaninn.paymentservice.model.BookingEventType;
import io.github.vylegzhaninn.paymentservice.model.PaymentStatus;
import io.github.vylegzhaninn.paymentservice.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "payment.success-rate=1.0",
    "payment.processing-delay=0ms"
})
class PaymentFlowIntegrationTest {

    private static final String USER_NAME = "ivan_ivanov";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.0.0"));

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void bookingCreatedEvent_chargesPaymentAndPublishesResult() throws Exception {
        var event = bookingEvent(BookingEventType.BOOKING_CREATED, 100L);

        sendBookingEvent(event);

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(paymentRepository.findSucceededByBookingId(100L)).isPresent()
        );
        var record = receivePaymentEventFor(100L);
        assertThat(record.value())
            .contains("PAYMENT_SUCCEEDED")
            .contains(USER_NAME);
    }

    @Test
    void duplicateBookingCreatedEvent_isIgnored() throws Exception {
        var event = bookingEvent(BookingEventType.BOOKING_CREATED, 200L);

        sendBookingEvent(event);
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(paymentRepository.findSucceededByBookingId(200L)).isPresent()
        );

        sendBookingEvent(event);

        Awaitility.await().during(Duration.ofSeconds(2)).untilAsserted(() ->
            assertThat(paymentRepository.findByBookingIdAndUserName(200L, USER_NAME)).hasSize(1)
        );
    }

    @Test
    void bookingCancelledEvent_refundsSucceededPayment() throws Exception {
        sendBookingEvent(bookingEvent(BookingEventType.BOOKING_CREATED, 300L));
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(paymentRepository.findSucceededByBookingId(300L)).isPresent()
        );

        sendBookingEvent(bookingEvent(BookingEventType.BOOKING_CANCELLED, 300L));

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            var payment = paymentRepository.findSucceededByBookingId(300L);
            assertThat(payment).isEmpty();
            assertThat(paymentRepository.findByBookingIdAndUserName(300L, USER_NAME))
                .singleElement()
                .satisfies(p -> assertThat(p.status()).isEqualTo(PaymentStatus.REFUNDED));
        });
    }

    private BookingEvent bookingEvent(BookingEventType type, long bookingId) {
        return new BookingEvent(
            type, bookingId, 1L, "Grand Palace", "101", USER_NAME,
            LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
            new BigDecimal("9000.00"), Instant.now()
        );
    }

    private void sendBookingEvent(BookingEvent event) throws Exception {
        try (Producer<String, String> producer = newProducer()) {
            producer.send(new ProducerRecord<>("booking-events", String.valueOf(event.bookingId()),
                objectMapper.writeValueAsString(event))).get();
        }
    }

    private ConsumerRecord<String, String> receivePaymentEventFor(long bookingId) {
        try (Consumer<String, String> consumer = newConsumer()) {
            consumer.subscribe(List.of("payment-events"));
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                for (var record : consumer.poll(Duration.ofSeconds(2))) {
                    if (record.value().contains(String.valueOf(bookingId))) {
                        return record;
                    }
                }
            }
            throw new AssertionError("Не получено событие payment-events по бронированию " + bookingId);
        }
    }

    private Producer<String, String> newProducer() {
        return new KafkaProducer<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        ));
    }

    private Consumer<String, String> newConsumer() {
        return new KafkaConsumer<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, "payment-flow-test-" + System.nanoTime(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        ));
    }
}
