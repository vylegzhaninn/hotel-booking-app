package io.github.vylegzhaninn.gateway.controller;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestControllerAdvice
public class AuthenticationExceptionController {

    private final ObjectMapper objectMapper;

    public AuthenticationExceptionController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException e) {
        if (e.status() <= 0) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "auth-service недоступен");
        }
        HttpStatus status = HttpStatus.valueOf(e.status());
        return ProblemDetail.forStatusAndDetail(status, detail(e, status));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ProblemDetail handleCircuitOpen(CallNotPermittedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
            "auth-service недоступен: circuit breaker открыт, повторите позже");
    }

    private String detail(FeignException e, HttpStatus status) {
        try {
            JsonNode body = objectMapper.readTree(e.contentUTF8());
            if (body.hasNonNull("detail")) {
                return body.get("detail").asText();
            }
        } catch (Exception ignored) {
            // тело ответа не JSON — используем сообщение по умолчанию
        }
        return switch (status) {
            case UNAUTHORIZED -> "Неверное имя пользователя или пароль";
            case CONFLICT -> "Пользователь с таким именем уже существует";
            default -> "Некорректный запрос";
        };
    }
}
