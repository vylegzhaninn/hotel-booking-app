package io.github.vylegzhaninn.paymentservice.service;

import io.github.vylegzhaninn.paymentservice.messaging.PaymentEventPublisher;
import io.github.vylegzhaninn.paymentservice.model.BookingEvent;
import io.github.vylegzhaninn.paymentservice.model.Payment;
import io.github.vylegzhaninn.paymentservice.model.PaymentEvent;
import io.github.vylegzhaninn.paymentservice.model.PaymentEventType;
import io.github.vylegzhaninn.paymentservice.model.PaymentStatus;
import io.github.vylegzhaninn.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayImitation paymentGateway;
    private final PaymentEventPublisher eventPublisher;

    public void handleBookingEvent(BookingEvent event) {
        switch (event.type()) {
            case BOOKING_CREATED -> pay(event);
            case BOOKING_CANCELLED -> refund(event);
            case BOOKING_CONFIRMED -> log.debug("Бронирование {} подтверждено, от платежа ничего не требуется",
                event.bookingId());
        }
    }

    /**
     * Kafka гарантирует at-least-once, поэтому повторная доставка BOOKING_CREATED
     * не должна списывать деньги второй раз — попытка обрабатывается один раз.
     */
    private void pay(BookingEvent event) {
        if (paymentRepository.existsByBookingId(event.bookingId())) {
            log.info("Платёж по бронированию {} уже выполнялся, пропуск повторного события",
                event.bookingId());
            return;
        }
        PaymentStatus status = paymentGateway.process(event.totalAmount());
        Payment payment = paymentRepository.insert(
            event.bookingId(), event.userName(), event.totalAmount(), status
        );
        eventPublisher.publish(new PaymentEvent(
            status == PaymentStatus.SUCCEEDED ? PaymentEventType.PAYMENT_SUCCEEDED : PaymentEventType.PAYMENT_FAILED,
            payment.id(),
            payment.bookingId(),
            payment.userName(),
            payment.amount(),
            Instant.now()
        ));
    }

    private void refund(BookingEvent event) {
        var succeeded = paymentRepository.findSucceededByBookingId(event.bookingId());
        if (succeeded.isEmpty()) {
            log.debug("По бронированию {} нет успешного платежа — возвращать нечего", event.bookingId());
            return;
        }
        paymentRepository.markRefunded(event.bookingId());
        var payment = succeeded.orElseThrow();
        eventPublisher.publish(new PaymentEvent(
            PaymentEventType.PAYMENT_REFUNDED,
            payment.id(),
            payment.bookingId(),
            payment.userName(),
            payment.amount(),
            Instant.now()
        ));
    }
}
