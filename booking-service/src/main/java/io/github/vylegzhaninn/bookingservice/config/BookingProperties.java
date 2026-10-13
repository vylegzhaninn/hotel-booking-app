package io.github.vylegzhaninn.bookingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Параметры бронирования.
 *
 * @param holdTtl сколько времени номер удерживается за пользователем до оплаты
 */
@ConfigurationProperties(prefix = "booking")
public record BookingProperties(Duration holdTtl) {
}
