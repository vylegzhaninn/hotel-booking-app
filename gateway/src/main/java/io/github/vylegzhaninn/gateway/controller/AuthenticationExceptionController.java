package io.github.vylegzhaninn.gateway.controller;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class AuthenticationExceptionController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String detail = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ProblemDetail handleUnauthorized(FeignException.Unauthorized ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Неверное имя пользователя или пароль");
    }

    @ExceptionHandler(FeignException.Conflict.class)
    public ProblemDetail handleConflict(FeignException.Conflict ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Пользователь с таким именем уже существует");
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException ex) {
        HttpStatus status =
                ex.status() > 0 ? HttpStatus.valueOf(ex.status()) : HttpStatus.SERVICE_UNAVAILABLE;
        String detail =
                ex.contentUTF8().isBlank() ? "Сервис аутентификации недоступен" : ex.contentUTF8();
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
