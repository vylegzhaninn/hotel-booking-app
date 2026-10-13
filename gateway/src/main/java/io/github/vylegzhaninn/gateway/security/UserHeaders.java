package io.github.vylegzhaninn.gateway.security;

/**
 * Заголовки, по которым шлюз передаёт проверенные данные пользователя сервисам.
 */
public final class UserHeaders {

    public static final String USER_NAME = "X-User-Name";
    public static final String USER_ROLE = "X-User-Role";

    private UserHeaders() {
    }
}
