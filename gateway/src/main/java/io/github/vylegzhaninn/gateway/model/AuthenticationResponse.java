package io.github.vylegzhaninn.gateway.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ аутентификации")
public record AuthenticationResponse(
    @Schema(
        description = "Access-токен (JWT)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    String accessToken
) {
}
