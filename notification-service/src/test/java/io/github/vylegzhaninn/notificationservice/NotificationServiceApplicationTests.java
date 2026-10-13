package io.github.vylegzhaninn.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties =
    "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration")
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
