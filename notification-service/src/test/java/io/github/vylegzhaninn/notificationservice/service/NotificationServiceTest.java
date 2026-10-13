package io.github.vylegzhaninn.notificationservice.service;

import io.github.vylegzhaninn.notificationservice.channel.NotificationChannel;
import io.github.vylegzhaninn.notificationservice.model.BookingEvent;
import io.github.vylegzhaninn.notificationservice.model.BookingEventType;
import io.github.vylegzhaninn.notificationservice.model.Notification;
import io.github.vylegzhaninn.notificationservice.model.NotificationType;
import io.github.vylegzhaninn.notificationservice.model.PaymentEvent;
import io.github.vylegzhaninn.notificationservice.model.PaymentEventType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    private final CapturingChannel emailChannel = new CapturingChannel();
    private final CapturingChannel webhookChannel = new CapturingChannel();

    private final NotificationService notificationService =
        new NotificationService(List.of(emailChannel, webhookChannel));

    @Test
    void bookingCreatedEvent_isDeliveredToAllChannels() {
        notificationService.onBookingEvent(bookingEvent(BookingEventType.BOOKING_CREATED));

        assertThat(emailChannel.sent).hasSize(1);
        assertThat(webhookChannel.sent).hasSize(1);
        var notification = emailChannel.sent.getFirst();
        assertThat(notification.type()).isEqualTo(NotificationType.BOOKING_CREATED);
        assertThat(notification.recipient()).isEqualTo("ivan_ivanov@hotel.local");
        assertThat(notification.subject()).contains("создано");
        assertThat(notification.body()).contains("Grand Palace").contains("101");
    }

    @Test
    void paymentSucceededEvent_isNotDelivered() {
        notificationService.onPaymentEvent(new PaymentEvent(
            PaymentEventType.PAYMENT_SUCCEEDED, 1L, 1L, "ivan_ivanov", new BigDecimal("9000.00"), Instant.now()
        ));

        assertThat(emailChannel.sent).isEmpty();
        assertThat(webhookChannel.sent).isEmpty();
    }

    @Test
    void paymentFailedEvent_notifiesUser() {
        notificationService.onPaymentEvent(new PaymentEvent(
            PaymentEventType.PAYMENT_FAILED, 1L, 1L, "ivan_ivanov", new BigDecimal("9000.00"), Instant.now()
        ));

        assertThat(emailChannel.sent).singleElement().satisfies(notification -> {
            assertThat(notification.type()).isEqualTo(NotificationType.PAYMENT_FAILED);
            assertThat(notification.subject()).contains("не прошёл");
        });
    }

    private BookingEvent bookingEvent(BookingEventType type) {
        return new BookingEvent(
            type, 1L, 1L, "Grand Palace", "101", "ivan_ivanov",
            LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
            new BigDecimal("9000.00"), Instant.now()
        );
    }

    private static final class CapturingChannel implements NotificationChannel {

        private final List<Notification> sent = new ArrayList<>();

        @Override
        public String name() {
            return "capturing";
        }

        @Override
        public void send(Notification notification) {
            sent.add(notification);
        }
    }
}
