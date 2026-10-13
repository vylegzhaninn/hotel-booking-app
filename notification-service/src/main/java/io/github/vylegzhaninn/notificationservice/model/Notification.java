package io.github.vylegzhaninn.notificationservice.model;

import java.time.Instant;

/**
 * Уведомление, разносимое по всем включённым каналам.
 */
public record Notification(
    NotificationType type,
    String recipient,
    String subject,
    String body,
    Instant occurredAt
) {
}
