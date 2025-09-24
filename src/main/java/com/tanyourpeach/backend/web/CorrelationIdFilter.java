package com.tanyourpeach.backend.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.UUID;

public final class CorrelationIdFilter implements Filter {
    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    private static final int MAX_LEN = 100; // protect logs from huge header values

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;
        String id = r.getHeader(HEADER);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        } else {
            id = id.trim();
            if (id.length() > MAX_LEN) id = id.substring(0, MAX_LEN);
        }
        MDC.put(MDC_KEY, id);
        r.setAttribute(MDC_KEY, id); // convenient for controllers/tests if needed
        w.setHeader(HEADER, id);     // always echo back
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}