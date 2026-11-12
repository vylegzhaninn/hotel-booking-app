package io.github.vylegzhaninn.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Пропускает через шлюз только запросы к API с действительным JWT
 * и прокидывает проверенные данные пользователя вниз по цепочке.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORITIES_CLAIM = "authorities";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtVerifier jwtVerifier;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(API_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        var token = bearerToken(request);
        if (token == null) {
            unauthorized(response, "Требуется заголовок Authorization: Bearer <token>");
            return;
        }
        try {
            var claims = jwtVerifier.verify(token);
            chain.doFilter(new UserHeadersRequestWrapper(request, claims.getSubject(), role(claims)), response);
        } catch (JwtException e) {
            unauthorized(response, "Недействительный или просроченный токен");
        }
    }

    private String bearerToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String role(Claims claims) {
        var authorities = (List<String>) claims.get(AUTHORITIES_CLAIM, List.class);
        if (authorities == null || authorities.isEmpty()) {
            return "USER";
        }
        var authority = authorities.getFirst();
        return authority.startsWith(ROLE_PREFIX) ? authority.substring(ROLE_PREFIX.length()) : authority;
    }

    private void unauthorized(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
            ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail)));
    }
}
