package io.github.vylegzhaninn.gateway.config;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j для вызова auth-service через Feign: circuit breaker на все
 * вызовы и retry только сетевых сбоев (4xx/5xx ответы не повторяем).
 */
@Configuration
public class ResilienceConfig {

    private static final String AUTH_CLIENT_RETRY = "auth-client";

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCircuitBreaker() {
        var circuitBreaker = CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();
        var timeLimiter = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5))
            .build();
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(circuitBreaker)
            .timeLimiterConfig(timeLimiter)
            .build());
    }

    @Bean
    public Retry authClientRetry() {
        return Retry.of(AUTH_CLIENT_RETRY, RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(IntervalFunction.ofExponentialBackoff(200, 2.0))
            .retryOnException(e -> e instanceof FeignException feign && feign.status() <= 0)
            .build());
    }
}
