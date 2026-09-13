package com.github.guifelipem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthRateLimitFilterTest {

    @Test
    void shouldLimitLoginByIpAndResetAfterWindow() throws Exception {
        MutableClock clock = new MutableClock();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        AuthRateLimitFilter filter = new AuthRateLimitFilter(mapper, 2, 1, 60, clock);

        assertEquals(200, execute(filter, "/api/auth/login").getStatus());
        assertEquals(200, execute(filter, "/api/auth/login").getStatus());
        MockHttpServletResponse blocked = execute(filter, "/api/auth/login");
        assertEquals(429, blocked.getStatus());
        assertEquals("60", blocked.getHeader("Retry-After"));

        clock.advanceSeconds(60);
        assertEquals(200, execute(filter, "/api/auth/login").getStatus());
    }

    private MockHttpServletResponse execute(AuthRateLimitFilter filter, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private static final class MutableClock extends Clock {
        private final AtomicLong millis = new AtomicLong();

        void advanceSeconds(long seconds) {
            millis.addAndGet(seconds * 1_000);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis.get());
        }

        @Override
        public long millis() {
            return millis.get();
        }
    }
}
