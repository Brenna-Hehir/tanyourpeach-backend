package com.tanyourpeach.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerStandaloneTest {

    @Test
    void runtimeException_returns400() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<?> resp = handler.handleRuntimeException(new RuntimeException("boom"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isInstanceOf(String.class);
        assertThat((String) resp.getBody()).contains("boom");
    }
}