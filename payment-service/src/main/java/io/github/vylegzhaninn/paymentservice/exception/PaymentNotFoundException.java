package io.github.vylegzhaninn.paymentservice.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(long bookingId) {
        super("Платежи по бронированию не найдены: " + bookingId);
    }
}
