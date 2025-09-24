package com.tanyourpeach.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;

public final class JsonAuthHandlers {
        private JsonAuthHandlers() {} // util class

    public static AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper om) {
        return (request, response, authException) -> write(response, request, om, HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    public static AccessDeniedHandler accessDeniedHandler(ObjectMapper om) {
        return (request, response, accessDeniedException) -> write(response, request, om, HttpStatus.FORBIDDEN, "Access denied");
    }

    private static void write(HttpServletResponse res, HttpServletRequest req, ObjectMapper om,
                              HttpStatus status, String message) throws IOException {
        if (res.isCommitted()) return;

        // Correlation id from MDC (set by CorrelationIdFilter)
        String cid = MDC.get("correlationId");

        ErrorResponse body = new ErrorResponse();
        body.timestamp = Instant.now();
        body.status = status.value();
        body.error = status.getReasonPhrase();
        body.message = message;
        body.path = req.getRequestURI();
        body.method = req.getMethod();
        body.correlationId = cid;

        // Headers
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
        if (cid != null && !cid.isBlank()) {
            res.setHeader("X-Correlation-Id", cid);
        }
        // Ensure no basic-auth challenge header sneaks in
        res.setHeader("WWW-Authenticate", "");
        om.writeValue(res.getOutputStream(), body);
        res.flushBuffer();
    }
}