package io.github.vylegzhaninn.gateway.controller;

import io.github.vylegzhaninn.gateway.client.AuthenticationClient;
import io.github.vylegzhaninn.gateway.model.AuthenticationRequest;
import io.github.vylegzhaninn.gateway.model.AuthenticationResponse;
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

@Tag(name = "Authentication", description = "Регистрация, вход и выход из системы")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationClient authenticationClient;

    @Operation(summary = "Вход пользователя", description = "Аутентифицирует пользователя и " +
            "возвращает access-токен")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса"),
            @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль")
    })
    @GetMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody AuthenticationRequest request) {
        return authenticationClient.login(request);
    }

    @Operation(summary = "Регистрация пользователя", description = "Создаёт нового пользователя и " +
            "возвращает access-токен")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким именем уже существует")
    })
    @PostMapping("/registration")
    public AuthenticationResponse registration(@Valid @RequestBody AuthenticationRequest request) {
        return authenticationClient.registration(request);
    }

    @Operation(summary = "Выход из системы", description = "Завершает сессию пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный выход"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса")
    })
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody AuthenticationRequest request) {
        authenticationClient.logout(request);
    }
}
