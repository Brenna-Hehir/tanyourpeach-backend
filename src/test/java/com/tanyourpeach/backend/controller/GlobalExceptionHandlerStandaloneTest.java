package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerStandaloneTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        // Build MockMvc with ONLY our dummy controller + your @ControllerAdvice.
        mvc = MockMvcBuilders
                .standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                // ensure bean validation triggers on @Valid @RequestBody
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    // Tiny controller to trigger the advice paths
    @RestController
    static class DummyController {
        @PostMapping("/__test__/validate")
        public String validate(@Valid @RequestBody Input input) {
            return "ok";
        }
        @GetMapping("/__test__/boom")
        public String boom() { throw new RuntimeException("kaboom"); }

        static class Input {
            @NotBlank(message = "name is required")
            public String name;
        }
    }

    @Test
    @DisplayName("MethodArgumentNotValidException → 400 (validation error)")
    void validationError_returns400() throws Exception {
        String body = "{}"; // missing 'name'
        mvc.perform(post("/__test__/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isBadRequest());
           // If your handler returns a specific JSON structure, add jsonPath checks here.
    }

    @Test
    @DisplayName("RuntimeException → 500 (generic error)")
    void runtimeException_returns500() throws Exception {
        mvc.perform(get("/__test__/boom"))
           .andExpect(status().isInternalServerError());
           // Add jsonPath checks if GlobalExceptionHandler sets body fields.
    }
}