package io.github.vylegzhaninn.gateway.service;

import io.github.resilience4j.retry.Retry;
import io.github.vylegzhaninn.gateway.client.AuthenticationClient;
import io.github.vylegzhaninn.gateway.model.AuthenticationRequest;
import io.github.vylegzhaninn.gateway.model.AuthenticationResponse;
import org.springframework.stereotype.Service;

/**
 * Оборачивает вызовы auth-service в retry на сетевые сбои.
 */
@Service
public class AuthenticationService {

    private final AuthenticationClient authenticationClient;
    private final Retry retry;

    public AuthenticationService(AuthenticationClient authenticationClient, Retry authClientRetry) {
        this.authenticationClient = authenticationClient;
        this.retry = authClientRetry;
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        return retry.executeSupplier(() -> authenticationClient.login(request));
    }

    public AuthenticationResponse registration(AuthenticationRequest request) {
        return retry.executeSupplier(() -> authenticationClient.registration(request));
    }
}
