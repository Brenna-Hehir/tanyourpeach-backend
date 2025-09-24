package com.tanyourpeach.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.NestedExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import java.sql.SQLException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400: bean validation @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldErrorItem> fields = ex.getBindingResult().getFieldErrors()
                .stream().map(f -> new ErrorResponse.FieldErrorItem(f.getField(), f.getDefaultMessage()))
                .collect(Collectors.toList());
        return build(req, HttpStatus.BAD_REQUEST, "Field validation failed", fields, false, ex);
    }

    // 400: @Validated on params/path/query
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldErrorItem> fields = ex.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.FieldErrorItem(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());
        return build(req, HttpStatus.BAD_REQUEST, "Constraint violation", fields, false, ex);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVar(org.springframework.web.bind.MissingPathVariableException ex,
                                                            HttpServletRequest req) {
        return build(req, HttpStatus.BAD_REQUEST, "Missing path variable: " + ex.getVariableName(), null, false, ex);
    }

    @ExceptionHandler({jakarta.persistence.EntityNotFoundException.class,
                   org.springframework.dao.EmptyResultDataAccessException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(req, HttpStatus.NOT_FOUND, "Resource not found", null, false, ex);
    }

    // 400: binding problems (e.g., query/form)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldErrorItem> fields = ex.getBindingResult().getFieldErrors()
                .stream().map(f -> new ErrorResponse.FieldErrorItem(f.getField(), f.getDefaultMessage()))
                .collect(Collectors.toList());
        return build(req, HttpStatus.BAD_REQUEST, "Binding failed", fields, false, ex);
    }

    // 400: missing required param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(req, HttpStatus.BAD_REQUEST, "Missing parameter: " + ex.getParameterName(), null, false, ex);
    }

    // 400: unreadable/malformed JSON
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(req, HttpStatus.BAD_REQUEST, "Malformed request body", null, false, ex);
    }

    // 400: /id should be number, etc.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return build(req, HttpStatus.BAD_REQUEST, "Type mismatch for parameter: " + ex.getName(), null, false, ex);
    }

    // 409 (or 400): DB constraint/duplicate
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);

        boolean duplicate = false;
        String msg = "Data integrity violation";

        // Hibernate constraint violations (if available)
        if (root instanceof org.hibernate.exception.ConstraintViolationException h) {
            String constraint = h.getConstraintName();
            if (constraint != null && constraint.toLowerCase().contains("unique")) {
                duplicate = true;
                msg = "Duplicate resource";
            }
            // also check SQLState inside Hibernate exception
            String state = h.getSQLState();
            if ("23505".equals(state) || "23000".equals(state)) {
                duplicate = true;
                msg = "Duplicate resource";
            }
        } else if (root instanceof java.sql.SQLIntegrityConstraintViolationException) {
            duplicate = true;
            msg = "Duplicate resource";
        } else if (root instanceof SQLException sql) {
            String state = sql.getSQLState();
            if ("23505".equals(state) || "23000".equals(state)) {
                duplicate = true;
                msg = "Duplicate resource";
            }
        }

        HttpStatus status = duplicate ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return build(req, status, msg, null, false, ex);
    }

    // 403: security
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(req, HttpStatus.FORBIDDEN, "Access denied", null, false, ex);
    }

    // Honor explicit statuses
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(req, status, ex.getReason(), null, false, ex);
    }

    // 405/415: useful but optional
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(req, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", null, false, ex);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return build(req, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type", null, false, ex);
    }

    // Catch-all: true 500 (generic message only)
    @ExceptionHandler({RuntimeException.class, Exception.class})
    public ResponseEntity<ErrorResponse> handleRuntime(Exception ex, HttpServletRequest req) {
        return build(req, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", null, true, ex);
    }

    private ResponseEntity<ErrorResponse> build(HttpServletRequest req,
                                                HttpStatus status,
                                                String message,
                                                List<ErrorResponse.FieldErrorItem> fields,
                                                boolean logStack,
                                                Exception ex) {
        String correlationId = org.slf4j.MDC.get("correlationId");
        if (status.is5xxServerError()) {
            if (logStack) log.error("[{}] {}", correlationId, ex.toString(), ex);
            else log.error("[{}] {}", correlationId, ex.toString());
        } else {
            log.info("[{}] {} -> {}", correlationId, status.value(), message);
        }

        ErrorResponse body = new ErrorResponse();
        body.timestamp = Instant.now();
        body.status = status.value();
        body.error = status.getReasonPhrase();
        body.message = (message == null || message.isBlank()) ? status.getReasonPhrase() : message;
        body.path = req.getRequestURI();
        body.method = req.getMethod();
        body.correlationId = correlationId;
        body.fieldErrors = (fields == null || fields.isEmpty()) ? null : fields;

        return ResponseEntity.status(status).body(body);
    }
}