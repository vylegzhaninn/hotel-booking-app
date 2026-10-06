package io.github.vylegzhaninn.authservice.controller;

import io.github.vylegzhaninn.authservice.model.AuthenticationRequest;
import io.github.vylegzhaninn.authservice.model.AuthenticationResponse;
import io.github.vylegzhaninn.authservice.service.UserAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Регистрация и вход в систему")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserAuthenticationService userAuthenticationService;

    @Operation(summary = "Вход пользователя", description = "Аутентифицирует пользователя и возвращает access-токен")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Успешный вход"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса"),
        @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль")
    })
    @GetMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody AuthenticationRequest request) {
        return userAuthenticationService.login(request);
    }

    @Operation(summary = "Регистрация пользователя", description = "Создаёт нового пользователя и возвращает access-токен")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса"),
        @ApiResponse(responseCode = "409", description = "Пользователь с таким именем уже существует")
    })
    @PostMapping("/registration")
    public AuthenticationResponse registration(@Valid @RequestBody AuthenticationRequest request) {
        return userAuthenticationService.registration(request);
    }
}
