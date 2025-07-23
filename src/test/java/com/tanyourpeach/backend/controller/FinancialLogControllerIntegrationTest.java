package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.FinancialLog.Type;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FinancialLogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FinancialLogRepository financialLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    private String userToken;
    
    private FinancialLog testLog;

    @BeforeEach
    void setUp() {
        financialLogRepository.deleteAll();
        userRepository.deleteAll();

        // Create test admin user
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("pass");
        admin.setIsAdmin(true);
        userRepository.save(admin);

        // Create test non-admin user
        User user = new User();
        user.setName("User");
        user.setEmail("user@example.com");
        user.setPasswordHash("pass");
        user.setIsAdmin(false);
        userRepository.save(user);

        // Generate tokens
        adminToken = "Bearer " + jwtService.generateToken(admin);
        userToken = "Bearer " + jwtService.generateToken(user);

        // Create a test log entry
        testLog = new FinancialLog();
        testLog.setType(Type.revenue);
        testLog.setSource("Test source");
        testLog.setAmount(BigDecimal.valueOf(150.00));
        testLog.setDescription("Test description");
        financialLogRepository.save(testLog);
    }

    @Test
    void getAllLogs_shouldSucceed_forAdmin() throws Exception {
        mockMvc.perform(get("/api/financial-log")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].logId").value(testLog.getLogId()));
    }

    @Test
    void getAllLogs_shouldFail_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/financial-log")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied"));
    }

    @Test
    void getLogById_shouldReturnLog_forAdmin() throws Exception {
        mockMvc.perform(get("/api/financial-log/" + testLog.getLogId())
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logId").value(testLog.getLogId()));
    }

    @Test
    void getLogById_shouldReturn404_ifNotFound() throws Exception {
        mockMvc.perform(get("/api/financial-log/9999")
                .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLogById_shouldFail_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/financial-log/" + testLog.getLogId())
                .header("Authorization", userToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied"));
    }
}