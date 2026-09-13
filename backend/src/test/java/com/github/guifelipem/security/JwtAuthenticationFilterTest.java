package com.github.guifelipem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @Test
    void shouldRejectPreviouslyIssuedJwtWhenUserIsBlocked() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService,
                new ObjectMapper().findAndRegisterModules()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer issued-before-block");

        when(jwtService.extractEmail("issued-before-block")).thenReturn("blocked@example.com");
        when(userDetailsService.loadUserByUsername("blocked@example.com")).thenReturn(
                User.withUsername("blocked@example.com")
                        .password("hash")
                        .roles("CLIENT")
                        .disabled(true)
                        .build()
        );

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Usuário bloqueado"));
        verify(jwtService, never()).isTokenValid("issued-before-block");
        verify(filterChain, never()).doFilter(request, response);
    }
}
