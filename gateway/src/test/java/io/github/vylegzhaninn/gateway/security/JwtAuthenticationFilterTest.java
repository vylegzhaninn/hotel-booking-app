package io.github.vylegzhaninn.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "dev-only-secret-key-please-change-32chars!";
    private static final String USER_NAME = "ivan_ivanov";

    private JwtAuthenticationFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(new JwtVerifier(SECRET), new JsonMapper());
        chain = mock(FilterChain.class);
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        var request = apiRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void requestWithInvalidTokenIsRejected() throws Exception {
        var request = apiRequest();
        request.addHeader("Authorization", "Bearer not-a-jwt");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void validTokenAddsUserHeaders() throws Exception {
        var request = apiRequest();
        // клиент пытается подделать личность — шлюз обязан это перекрыть
        request.addHeader(UserHeaders.USER_NAME, "attacker");
        request.addHeader("Authorization", "Bearer " + validToken());
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        var forwarded = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(forwarded.capture(), any());
        assertThat(forwarded.getValue().getHeader(UserHeaders.USER_NAME)).isEqualTo(USER_NAME);
        assertThat(forwarded.getValue().getHeader(UserHeaders.USER_ROLE)).isEqualTo("USER");
    }

    @Test
    void nonApiRequestPassesThrough() throws Exception {
        var request = new MockHttpServletRequest("POST", "/login");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    private MockHttpServletRequest apiRequest() {
        return new MockHttpServletRequest("GET", "/api/bookings");
    }

    private String validToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        var now = Instant.now();
        return Jwts.builder()
            .subject(USER_NAME)
            .claim("authorities", List.of("ROLE_USER"))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(3600)))
            .signWith(key)
            .compact();
    }
}
