package com.tanyourpeach.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // filters ON (default)
class ErrorShapeWithFiltersIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void notFound_shouldIncludeCorrelationId_headerAndBody() throws Exception {
        mockMvc.perform(get("/api/users/999999")) // public route; no auth header
            .andExpect(status().isNotFound())
            // header from CorrelationIdFilter
            .andExpect(header().exists("X-Correlation-Id"))
            // body fields
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.correlationId").exists())
            .andExpect(jsonPath("$.path").value("/api/users/999999"))
            .andExpect(jsonPath("$.method").value("GET"));
    }

    @Test
    void clientCanProvideCorrelationId_andServerEchoesIt() throws Exception {
        String provided = "test-corr-id-123";
        mockMvc.perform(get("/api/users/999998").header("X-Correlation-Id", provided))
            .andExpect(status().isNotFound())
            .andExpect(header().string("X-Correlation-Id", provided))
            .andExpect(jsonPath("$.correlationId").value(provided));
    }
}