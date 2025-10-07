package com.tanyourpeach.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, AccessDeniedException ex) throws IOException {
        resp.setStatus(HttpStatus.FORBIDDEN.value());
        resp.setContentType("application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        body.put("path", req.getRequestURI());
        String cid = req.getHeader("X-Correlation-Id");
        if (cid != null) body.put("correlationId", cid);
        resp.getWriter().write(om.writeValueAsString(body));
    }
}