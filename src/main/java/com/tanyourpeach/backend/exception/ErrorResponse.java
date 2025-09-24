package com.tanyourpeach.backend.exception;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timestamp", "status", "error", "message", "path", "method", "correlationId", "fieldErrors"
})
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    public Instant timestamp;
    public int status;
    public String error;
    public String message;
    public String path;
    public String method;
    public String correlationId;
    public List<FieldErrorItem> fieldErrors;

    public static class FieldErrorItem {
        public String field;
        public String message;
        public FieldErrorItem(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}