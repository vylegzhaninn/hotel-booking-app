package io.github.vylegzhaninn.notificationservice.channel;

import io.github.vylegzhaninn.notificationservice.model.Notification;

/**
 * Канал доставки уведомлений (email, webhook, ...).
 */
public interface NotificationChannel {

    String name();

    void send(Notification notification);
}
