package com.tanyourpeach.backend.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthentication_whenValidJwtProvided() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = new User("user@example.com", "password", Collections.emptyList());

        when(jwtService.extractUsername("validtoken")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("validtoken", userDetails)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth instanceof UsernamePasswordAuthenticationToken;
        assert auth.getName().equals("user@example.com");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthentication_whenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthentication_whenTokenIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("invalid")).thenReturn("user@example.com");
        UserDetails userDetails = new User("user@example.com", "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid", userDetails)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthentication_whenAuthorizationHeaderIsMalformed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token something"); // not Bearer
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthentication_whenUsernameIsNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer badtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("badtoken")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkip_whenAuthenticationAlreadyExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user@example.com", null, Collections.emptyList())
        );

        when(jwtService.extractUsername("validtoken")).thenReturn("user@example.com");

        jwtFilter.doFilterInternal(request, response, filterChain);

        // Should not overwrite existing auth
        assertEquals("user@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthentication_whenTokenIsEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer "); // only prefix, no token
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}