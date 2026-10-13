package io.github.vylegzhaninn.notificationservice.service;

import io.github.vylegzhaninn.notificationservice.channel.NotificationChannel;
import io.github.vylegzhaninn.notificationservice.model.BookingEvent;
import io.github.vylegzhaninn.notificationservice.model.Notification;
import io.github.vylegzhaninn.notificationservice.model.NotificationType;
import io.github.vylegzhaninn.notificationservice.model.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String EMAIL_DOMAIN = "@hotel.local";

    private final List<NotificationChannel> channels;

    /**
     * Успешный платёж отдельно не рассылается: booking-сервис публикует
     * BOOKING_CONFIRMED, которое и является хорошей новостью для пользователя.
     */
    public void onBookingEvent(BookingEvent event) {
        var notification = switch (event.type()) {
            case BOOKING_CREATED -> bookingCreated(event);
            case BOOKING_CONFIRMED -> bookingConfirmed(event);
            case BOOKING_CANCELLED -> bookingCancelled(event);
        };
        dispatch(notification);
    }

    public void onPaymentEvent(PaymentEvent event) {
        switch (event.type()) {
            case PAYMENT_FAILED -> dispatch(paymentFailed(event));
            case PAYMENT_REFUNDED -> dispatch(paymentRefunded(event));
            case PAYMENT_SUCCEEDED -> log.debug("PAYMENT_SUCCEEDED не рассылается: придёт BOOKING_CONFIRMED");
        }
    }

    private Notification bookingCreated(BookingEvent event) {
        return new Notification(
            NotificationType.BOOKING_CREATED,
            recipient(event.userName()),
            "Бронирование #%d создано".formatted(event.bookingId()),
            """
                Здравствуйте, %s!
                Номер %s в отеле «%s» забронирован на %s — %s.
                Сумма: %s руб. Оплатите бронирование до истечения холда, иначе оно будет отменено."""
                .formatted(event.userName(), event.roomNumber(), event.hotelName(),
                    event.checkIn(), event.checkOut(), event.totalAmount()),
            Instant.now()
        );
    }

    private Notification bookingConfirmed(BookingEvent event) {
        return new Notification(
            NotificationType.BOOKING_CONFIRMED,
            recipient(event.userName()),
            "Бронирование #%d подтверждено".formatted(event.bookingId()),
            "Здравствуйте, %s! Оплата получена, номер %s в отеле «%s» ждёт вас %s."
                .formatted(event.userName(), event.roomNumber(), event.hotelName(), event.checkIn()),
            Instant.now()
        );
    }

    private Notification bookingCancelled(BookingEvent event) {
        return new Notification(
            NotificationType.BOOKING_CANCELLED,
            recipient(event.userName()),
            "Бронирование #%d отменено".formatted(event.bookingId()),
            "Здравствуйте, %s! Бронирование номера %s в отеле «%s» отменено."
                .formatted(event.userName(), event.roomNumber(), event.hotelName()),
            Instant.now()
        );
    }

    private Notification paymentFailed(PaymentEvent event) {
        return new Notification(
            NotificationType.PAYMENT_FAILED,
            recipient(event.userName()),
            "Платёж по бронированию #%d не прошёл".formatted(event.bookingId()),
            "Здравствуйте, %s! Платёж на сумму %s руб. отклонён, бронирование отменено."
                .formatted(event.userName(), event.amount()),
            Instant.now()
        );
    }

    private Notification paymentRefunded(PaymentEvent event) {
        return new Notification(
            NotificationType.PAYMENT_REFUNDED,
            recipient(event.userName()),
            "Возврат средств по бронированию #%d".formatted(event.bookingId()),
            "Здравствуйте, %s! Платёж на сумму %s руб. возвращён."
                .formatted(event.userName(), event.amount()),
            Instant.now()
        );
    }

    private void dispatch(Notification notification) {
        channels.forEach(channel -> channel.send(notification));
    }

    private String recipient(String userName) {
        return userName.toLowerCase(Locale.ROOT) + EMAIL_DOMAIN;
    }
}
