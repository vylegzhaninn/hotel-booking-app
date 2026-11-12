package io.github.vylegzhaninn.paymentservice.mapper;

import io.github.vylegzhaninn.paymentservice.model.Payment;
import io.github.vylegzhaninn.paymentservice.model.PaymentResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
