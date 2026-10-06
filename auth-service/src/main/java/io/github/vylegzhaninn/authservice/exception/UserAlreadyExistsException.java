package io.github.vylegzhaninn.authservice.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String name) {
        super("Пользователь с таким именем уже существует: " + name);
    }
}
