package io.github.vylegzhaninn.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Параметры уведомлений.
 *
 * @param webhookUrl URL внешнего webhook-канала; пустая строка отключает канал
 */
@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(String webhookUrl) {
}
