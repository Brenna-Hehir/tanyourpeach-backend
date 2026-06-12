package com.tanyourpeach.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import com.tanyourpeach.backend.util.TestDataCleaner;
import org.junit.jupiter.api.BeforeEach;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // filters ON (default)
class ErrorShapeWithFiltersIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private TestDataCleaner testDataCleaner;

    private String adminToken;

    @BeforeEach
    void setup() {
        testDataCleaner.cleanAll();

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-error-shape@example.com");
        admin.setPasswordHash("adminpass");
        admin.setIsAdmin(true);
        userRepository.save(admin);

        adminToken = jwtService.generateToken(admin);
    }

    @Test
    void notFound_shouldIncludeCorrelationId_headerAndBody() throws Exception {
        mockMvc.perform(get("/api/users/999999")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound())
            .andExpect(header().exists("X-Correlation-Id"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.correlationId").exists())
            .andExpect(jsonPath("$.path").value("/api/users/999999"))
            .andExpect(jsonPath("$.method").value("GET"));
    }

    @Test
    void clientCanProvideCorrelationId_andServerEchoesIt() throws Exception {
        String provided = "test-corr-id-123";

        mockMvc.perform(get("/api/users/999998")
                .header("Authorization", "Bearer " + adminToken)
                .header("X-Correlation-Id", provided))
            .andExpect(status().isNotFound())
            .andExpect(header().string("X-Correlation-Id", provided))
            .andExpect(jsonPath("$.correlationId").value(provided));
    }
}