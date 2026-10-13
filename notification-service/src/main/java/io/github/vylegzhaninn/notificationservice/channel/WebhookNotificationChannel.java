package io.github.vylegzhaninn.notificationservice.channel;

import io.github.vylegzhaninn.notificationservice.config.NotificationProperties;
import io.github.vylegzhaninn.notificationservice.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Webhook-канал: отправляет уведомление POST-запросом на внешний URL.
 * Отключён, пока {@code notification.webhook-url} не задан; сбой доставки
 * не влияет на обработку событий.
 */
@Component
@Slf4j
public class WebhookNotificationChannel implements NotificationChannel {

    private final NotificationProperties properties;
    private final RestClient restClient;

    public WebhookNotificationChannel(NotificationProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String name() {
        return "webhook";
    }

    @Override
    public void send(Notification notification) {
        var url = properties.webhookUrl();
        if (url == null || url.isBlank()) {
            log.debug("Webhook-канал отключен: notification.webhook-url не задан");
            return;
        }
        try {
            restClient.post()
                .uri(url)
                .body(notification)
                .retrieve()
                .toBodilessEntity();
            log.info("Уведомление {} доставлено в webhook", notification.type());
        } catch (Exception e) {
            log.warn("Не удалось доставить уведомление {} в webhook {}: {}",
                notification.type(), url, e.getMessage());
        }
    }
}
