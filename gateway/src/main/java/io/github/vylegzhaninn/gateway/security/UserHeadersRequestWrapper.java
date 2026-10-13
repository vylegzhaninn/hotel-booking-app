package io.github.vylegzhaninn.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Добавляет в запрос заголовки пользователя, вычисленные из проверенного JWT,
 * и перекрывает одноимённые заголовки клиента — сервисы за шлюзом доверяют
 * только этим значениям.
 */
final class UserHeadersRequestWrapper extends HttpServletRequestWrapper {

    private static final Set<String> USER_HEADERS = Set.of(UserHeaders.USER_NAME, UserHeaders.USER_ROLE);

    private final Map<String, String> userHeaders;

    UserHeadersRequestWrapper(HttpServletRequest request, String userName, String role) {
        super(request);
        this.userHeaders = new HashMap<>();
        userHeaders.put(UserHeaders.USER_NAME, userName);
        userHeaders.put(UserHeaders.USER_ROLE, role);
    }

    @Override
    public String getHeader(String name) {
        if (USER_HEADERS.contains(name)) {
            return userHeaders.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (USER_HEADERS.contains(name)) {
            return Collections.enumeration(List.of(userHeaders.get(name)));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        var names = new LinkedHashSet<String>();
        var original = super.getHeaderNames();
        while (original.hasMoreElements()) {
            var name = original.nextElement();
            if (!USER_HEADERS.contains(name)) {
                names.add(name);
            }
        }
        names.addAll(userHeaders.keySet());
        return Collections.enumeration(names);
    }
}
