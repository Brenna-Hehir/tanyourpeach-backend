// File: backend/src/test/java/com/tanyourpeach/backend/controller/SecurityErrorShapeIntegrationTest.java
package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import com.tanyourpeach.backend.util.TestDataCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // filters ON
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityErrorShapeIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private TestDataCleaner testDataCleaner;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setup() {
        testDataCleaner.cleanAll();

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("adminpass");
        admin.setIsAdmin(true);
        userRepository.save(admin);
        adminToken = jwtService.generateToken(admin);

        User user = new User();
        user.setName("Not Admin");
        user.setEmail("user@example.com");
        user.setPasswordHash("userpass");
        user.setIsAdmin(false);
        userRepository.save(user);
        userToken = jwtService.generateToken(user);
    }

    @Test
    void adminRoute_withoutToken_returns401_withJsonShape_andCorrelationId() throws Exception {
        mockMvc.perform(get("/api/admin/stats/summary"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists("X-Correlation-Id"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Authentication required"))
            .andExpect(jsonPath("$.path").value("/api/admin/stats/summary"))
            .andExpect(jsonPath("$.method").value("GET"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void adminRoute_withNonAdminToken_returns403_withJsonShape_andCorrelationId() throws Exception {
        mockMvc.perform(get("/api/admin/stats/summary")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden())
            .andExpect(header().exists("X-Correlation-Id"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("Access denied"))
            .andExpect(jsonPath("$.path").value("/api/admin/stats/summary"))
            .andExpect(jsonPath("$.method").value("GET"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void adminRoute_withAdminToken_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/stats/summary")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    // empty Bearer → 401 JSON (and no chain)
    @org.junit.jupiter.api.Test
    void adminRoute_withEmptyBearer_returns401_withJsonShape() throws Exception {
        mockMvc.perform(get("/api/admin/stats/summary")
                .header("Authorization", "Bearer "))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.path").value("/api/admin/stats/summary"))
            .andExpect(jsonPath("$.method").value("GET"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // garbage token → 401 JSON
    @org.junit.jupiter.api.Test
    void adminRoute_withGarbageToken_returns401_withJsonShape() throws Exception {
        mockMvc.perform(get("/api/admin/stats/summary")
                .header("Authorization", "Bearer not-a-real-jwt"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.path").value("/api/admin/stats/summary"))
            .andExpect(jsonPath("$.method").value("GET"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // correlation id echoed on 401
    @org.junit.jupiter.api.Test
    void adminRoute_withoutToken_echoesCorrelationId_on401() throws Exception {
        String cid = "it-401-corr-123";
        mockMvc.perform(get("/api/admin/stats/summary")
                .header("X-Correlation-Id", cid))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("X-Correlation-Id", cid))
            .andExpect(jsonPath("$.correlationId").value(cid));
    }

    // correlation id echoed on 403
    @org.junit.jupiter.api.Test
    void adminRoute_withNonAdminToken_echoesCorrelationId_on403() throws Exception {
        String cid = "it-403-corr-456";
        mockMvc.perform(get("/api/admin/stats/summary")
                .header("Authorization", "Bearer " + userToken)
                .header("X-Correlation-Id", cid))
            .andExpect(status().isForbidden())
            .andExpect(header().string("X-Correlation-Id", cid))
            .andExpect(jsonPath("$.correlationId").value(cid));
    }

    // preflight OPTIONS should not be blocked
    @org.junit.jupiter.api.Test
    void adminRoute_preflightOptions_skipsAuth() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/admin/stats/summary")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk());
    }

    @org.junit.jupiter.api.Test
    void adminPost_withNonAdminToken_returns403() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/stats/rebuild")
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .content("{}")
        ).andExpect(status().isForbidden());
    }

    @org.junit.jupiter.api.Test
    void adminPost_withAdminToken_returns2xx() throws Exception {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/stats/rebuild")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{}")
        ).andExpect(status().is2xxSuccessful());
    }
}