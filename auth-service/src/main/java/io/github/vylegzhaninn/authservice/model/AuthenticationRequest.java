package io.github.vylegzhaninn.authservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос аутентификации")
public record AuthenticationRequest(
    @Schema(
        description = "Имя пользователя",
        example = "ivan_ivanov"
    )
    @NotBlank
    @Size(min = 3, max = 15)
    String name,
    @Schema(
        description = "Пароль: заглавная и строчная буквы, цифра и спецсимвол",
        example = "Str0ng@Pass"
    )
    @NotBlank
    @Size(min = 10, max = 30)
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).*$",
        message = "Пароль должен содержать заглавную, строчную букву, цифру и спецсимвол"
    )
    String password
) {
}
