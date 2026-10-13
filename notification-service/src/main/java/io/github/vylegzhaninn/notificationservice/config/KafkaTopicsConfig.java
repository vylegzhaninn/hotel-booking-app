package io.github.vylegzhaninn.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    public static final String BOOKING_EVENTS_TOPIC = "booking-events";
    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    @Bean
    public NewTopic bookingEvents() {
        return TopicBuilder.name(BOOKING_EVENTS_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentEvents() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC).partitions(1).replicas(1).build();
    }
}
