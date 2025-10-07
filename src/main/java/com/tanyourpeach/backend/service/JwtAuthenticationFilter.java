package com.tanyourpeach.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static com.tanyourpeach.backend.security.JsonAuthHandlers.authenticationEntryPoint;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    // provide a safe fallback for unit tests that don't wire Spring beans
    private ObjectMapper om() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper().findAndRegisterModules();
        }
        return objectMapper;
    }

    private static final AntPathMatcher PATHS = new AntPathMatcher();
    private static final String[] WHITELIST = {
        "/api/auth/**",
        "/actuator/health",
        "/error"
        // add swagger later if you enable it:
        // "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip CORS preflight and public endpoints
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        for (String p : WHITELIST) {
            if (PATHS.match(p, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No bearer token → let the entry point handle it later if endpoint requires auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7).trim();

        if (!StringUtils.hasText(jwt)) {
            authenticationEntryPoint(om())
                .commence(request, response,
                    new InsufficientAuthenticationException("Missing bearer token"));
            return;
        }

        // If already authenticated, skip re-auth
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String userEmail = jwtService.extractUsername(jwt);
            if (userEmail == null || userEmail.isBlank()) {
                // invalid token: emit 401 JSON and stop
                authenticationEntryPoint(om())
                    .commence(request, response,
                        new InsufficientAuthenticationException("Invalid token"));
                return;
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
            } else {
                authenticationEntryPoint(om())
                    .commence(request, response,
                        new InsufficientAuthenticationException("Invalid or expired token"));
            }
        } catch (Exception ex) {
            // Covers parsing errors, expired tokens, malformed JWT, etc.
            authenticationEntryPoint(om())
                .commence(request, response,
                    new InsufficientAuthenticationException("Invalid or expired token", ex));
        }
    }
}