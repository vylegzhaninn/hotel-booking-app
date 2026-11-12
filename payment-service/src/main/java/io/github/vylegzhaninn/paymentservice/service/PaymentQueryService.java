package io.github.vylegzhaninn.paymentservice.service;

import io.github.vylegzhaninn.paymentservice.exception.PaymentNotFoundException;
import io.github.vylegzhaninn.paymentservice.mapper.PaymentMapper;
import io.github.vylegzhaninn.paymentservice.model.PaymentResponse;
import io.github.vylegzhaninn.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public List<PaymentResponse> findByBookingId(String userName, long bookingId) {
        var payments = paymentRepository.findByBookingIdAndUserName(bookingId, userName);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException(bookingId);
        }
        return paymentMapper.toResponseList(payments);
    }
}
