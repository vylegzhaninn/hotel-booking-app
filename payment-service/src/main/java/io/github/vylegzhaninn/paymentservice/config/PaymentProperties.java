package io.github.vylegzhaninn.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Параметры имитации платёжного шлюза.
 *
 * @param successRate     доля успешных платежей, 0..1
 * @param processingDelay искусственная задержка «обработки» платежа
 */
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(double successRate, Duration processingDelay) {
}
