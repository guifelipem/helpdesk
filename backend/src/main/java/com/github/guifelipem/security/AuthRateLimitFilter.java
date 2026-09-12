package com.github.guifelipem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.guifelipem.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";

    private final ObjectMapper objectMapper;
    private final int loginLimit;
    private final int registerLimit;
    private final long windowMillis;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    public AuthRateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${app.rate-limit.auth.login-attempts:10}") int loginLimit,
            @Value("${app.rate-limit.auth.register-attempts:5}") int registerLimit,
            @Value("${app.rate-limit.auth.window-seconds:60}") long windowSeconds
    ) {
        this(objectMapper, loginLimit, registerLimit, windowSeconds, Clock.systemDefaultZone());
    }

    AuthRateLimitFilter(ObjectMapper objectMapper, int loginLimit, int registerLimit,
                        long windowSeconds, Clock clock) {
        this.objectMapper = objectMapper;
        this.loginLimit = loginLimit;
        this.registerLimit = registerLimit;
        this.windowMillis = windowSeconds * 1_000;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !LOGIN_PATH.equals(path) && !REGISTER_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long now = clock.millis();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        int limit = LOGIN_PATH.equals(path) ? loginLimit : registerLimit;
        String key = request.getRemoteAddr() + ':' + path;
        Window window = windows.computeIfAbsent(key, ignored -> new Window(now));

        long retryAfterMillis = window.recordAttempt(now, windowMillis, limit);
        if (retryAfterMillis > 0) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", Long.toString((retryAfterMillis + 999) / 1_000));
            objectMapper.writeValue(response.getWriter(), new ErrorResponse(
                    LocalDateTime.now(clock),
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Muitas tentativas. Tente novamente mais tarde"
            ));
            return;
        }

        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> entry.getValue().isExpired(now, windowMillis));
        }
        filterChain.doFilter(request, response);
    }

    private static final class Window {
        private long startedAt;
        private int attempts;

        private Window(long startedAt) {
            this.startedAt = startedAt;
        }

        private synchronized long recordAttempt(long now, long windowMillis, int limit) {
            if (now - startedAt >= windowMillis) {
                startedAt = now;
                attempts = 0;
            }
            if (attempts >= limit) {
                return windowMillis - (now - startedAt);
            }
            attempts++;
            return 0;
        }

        private synchronized boolean isExpired(long now, long windowMillis) {
            return now - startedAt >= windowMillis;
        }
    }
}
