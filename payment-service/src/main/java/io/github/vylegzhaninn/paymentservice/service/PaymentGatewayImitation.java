package io.github.vylegzhaninn.paymentservice.service;

import io.github.vylegzhaninn.paymentservice.config.PaymentProperties;
import io.github.vylegzhaninn.paymentservice.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Имитация внешнего платёжного шлюза: задержка обработки и вероятностный успех.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayImitation {

    private final PaymentProperties properties;

    public PaymentStatus process(BigDecimal amount) {
        try {
            Thread.sleep(properties.processingDelay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        var succeeded = ThreadLocalRandom.current().nextDouble() < properties.successRate();
        var status = succeeded ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
        log.info("Платёж на сумму {} руб. обработан: {}", amount, status);
        return status;
    }
}
