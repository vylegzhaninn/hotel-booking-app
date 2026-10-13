package io.github.vylegzhaninn.notificationservice.channel;

import io.github.vylegzhaninn.notificationservice.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Имитация email-канала: письмо выводится в лог сервиса.
 * Для реальной доставки достаточно заменить реализацию на SMTP-клиент.
 */
@Component
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public String name() {
        return "email";
    }

    @Override
    public void send(Notification notification) {
        log.info("""
            [EMAIL] Кому: {}
            Тема: {}
            Текст: {}\
            """, notification.recipient(), notification.subject(), notification.body());
    }
}
