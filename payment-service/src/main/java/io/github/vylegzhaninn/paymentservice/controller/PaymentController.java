package io.github.vylegzhaninn.paymentservice.controller;

import io.github.vylegzhaninn.paymentservice.model.PaymentResponse;
import io.github.vylegzhaninn.paymentservice.service.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Payments", description = "История платежей по бронированиям")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentQueryService paymentQueryService;

    @Operation(summary = "Платежи по бронированию", description = "Возвращает историю платежей текущего пользователя")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список платежей"),
        @ApiResponse(responseCode = "404", description = "Платежи по бронированию не найдены")
    })
    @GetMapping("/{bookingId}")
    public List<PaymentResponse> findByBookingId(
        @RequestHeader("X-User-Name") String userName,
        @PathVariable long bookingId
    ) {
        return paymentQueryService.findByBookingId(userName, bookingId);
    }
}
