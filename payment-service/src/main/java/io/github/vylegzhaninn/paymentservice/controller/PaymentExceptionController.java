package io.github.vylegzhaninn.paymentservice.controller;

import io.github.vylegzhaninn.paymentservice.exception.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionController {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail handlePaymentNotFound(PaymentNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
