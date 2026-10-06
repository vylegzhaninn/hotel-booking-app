package io.github.vylegzhaninn.authservice.model;

import lombok.Builder;

@Builder
public record User(
    Long id,
    String name,
    String password,
    Role role
) {
}
