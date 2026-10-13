package io.github.vylegzhaninn.bookingservice;

import tools.jackson.databind.ObjectMapper;
import io.github.vylegzhaninn.bookingservice.model.BookingResponse;
import io.github.vylegzhaninn.bookingservice.model.BookingStatus;
import io.github.vylegzhaninn.bookingservice.model.PaymentEvent;
import io.github.vylegzhaninn.bookingservice.model.PaymentEventType;
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
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
@TestPropertySource(properties = "eureka.client.enabled=false")
class BookingFlowIntegrationTest {

    private static final String USER_NAME = "ivan_ivanov";
    private static final long ROOM_STANDARD = 1L;
    private static final long ROOM_FAMILY = 4L;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:4.0.0"));

    @Container
    @ServiceConnection
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_holdsRoomAndPublishesEvent() {
        var checkIn = LocalDate.now().plusDays(30);
        var checkOut = checkIn.plusDays(2);

        var response = createBooking(ROOM_STANDARD, checkIn, checkOut);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var booking = response.getBody();
        assertThat(booking).isNotNull();
        assertThat(booking.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.totalAmount()).isEqualByComparingTo(new BigDecimal("9000.00"));
        assertThat(redisTemplate.hasKey("booking:hold:" + booking.id())).isTrue();

        var record = receiveBookingEventFor(booking.id());
        assertThat(record.value())
            .contains("BOOKING_CREATED")
            .contains(USER_NAME);
    }

    @Test
    void successfulPayment_confirmsBookingAndReleasesHold() throws Exception {
        var checkIn = LocalDate.now().plusDays(60);
        var checkOut = checkIn.plusDays(3);

        var booking = createBooking(ROOM_FAMILY, checkIn, checkOut).getBody();
        assertThat(booking).isNotNull();
        assertThat(redisTemplate.hasKey("booking:hold:" + booking.id())).isTrue();

        sendPaymentEvent(PaymentEventType.PAYMENT_SUCCEEDED, booking.id());

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            var bookings = findUserBookings();
            var confirmed = bookings.stream()
                .filter(b -> b.id().equals(booking.id()))
                .findFirst()
                .orElseThrow();
            assertThat(confirmed.status()).isEqualTo(BookingStatus.CONFIRMED);
        });
        assertThat(redisTemplate.hasKey("booking:hold:" + booking.id())).isFalse();
    }

    private ResponseEntity<BookingResponse> createBooking(long roomId, LocalDate checkIn, LocalDate checkOut) {
        var headers = new HttpHeaders();
        headers.add("X-User-Name", USER_NAME);
        var request = new HttpEntity<>(
            Map.of("roomId", roomId, "checkIn", checkIn.toString(), "checkOut", checkOut.toString()),
            headers
        );
        return restTemplate.postForEntity("/api/bookings", request, BookingResponse.class);
    }

    private List<BookingResponse> findUserBookings() {
        var headers = new HttpHeaders();
        headers.add("X-User-Name", USER_NAME);
        var response = restTemplate.exchange(
            "/api/bookings", HttpMethod.GET, new HttpEntity<Void>(headers), BookingResponse[].class
        );
        return List.of(response.getBody());
    }

    private void sendPaymentEvent(PaymentEventType type, long bookingId) throws Exception {
        var event = new PaymentEvent(type, 1L, bookingId, USER_NAME, new BigDecimal("9000.00"), Instant.now());
        try (Producer<String, String> producer = newProducer()) {
            producer.send(new ProducerRecord<>("payment-events", String.valueOf(bookingId),
                objectMapper.writeValueAsString(event))).get();
        }
    }

    private ConsumerRecord<String, String> receiveBookingEventFor(long bookingId) {
        try (Consumer<String, String> consumer = newConsumer()) {
            consumer.subscribe(List.of("booking-events"));
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                for (var record : consumer.poll(Duration.ofSeconds(2))) {
                    if (record.value().contains(String.valueOf(bookingId))) {
                        return record;
                    }
                }
            }
            throw new AssertionError("Не получено событие booking-events по бронированию " + bookingId);
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
            ConsumerConfig.GROUP_ID_CONFIG, "booking-flow-test-" + System.nanoTime(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        ));
    }
}
