package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private TestDataCleaner testDataCleaner;

    private User testUser;
    private String adminToken;
    private String nonAdminToken;

    @BeforeEach
    void setup() {
        testDataCleaner.cleanAll();

        User adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("adminpass");
        adminUser.setIsAdmin(true);
        adminUser.setAddress("Admin Address");
        userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpass");
        testUser.setAddress("123 Main St");
        testUser.setIsAdmin(false);
        testUser = userRepository.save(testUser);
        nonAdminToken = jwtService.generateToken(testUser);
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_shouldSucceed_withValidInput() throws Exception {
        // DTO payload (password field, not passwordHash)
        String payload = """
            {
              "name": "New User",
              "email": "newuser@example.com",
              "password": "newpass",
              "isAdmin": true,
              "address": "456 Other St"
            }
        """;

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void createUser_shouldReturn400_whenMissingEmail() throws Exception {
        String payload = """
            {"name":"No Email","password":"pass123"}
        """;

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Field validation failed"))
            .andExpect(jsonPath("$.path").value("/api/users"))
            .andExpect(jsonPath("$.method").value("POST"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").exists())
            .andExpect(jsonPath("$.fieldErrors[0].message").exists());
    }

    @Test
    void createUser_shouldReturn400_whenMissingPassword() throws Exception {
        String payload = """
            {
              "name": "No Pass",
              "email": "nopass@example.com"
            }
        """;

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400_whenEmailInvalidFormat() throws Exception {
        String payload = """
            {
              "name": "Bad Email",
              "email": "invalid-email",
              "password": "pass"
            }
        """;

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn409_whenDuplicateEmail() throws Exception {
        String payload = """
            {"name":"Duplicate","email":"test@example.com","password":"pass123","isAdmin":false}
        """;

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Duplicate resource"))
            .andExpect(jsonPath("$.path").value("/api/users"))
            .andExpect(jsonPath("$.method").value("POST"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateUser_shouldSucceed_withValidUpdate() throws Exception {
        String payload = """
            {
              "name": "Updated Name",
              "email": "updated@example.com",
              "password": "newSecret",
              "address": "New Address",
              "isAdmin": true
            }
        """;

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void updateUser_shouldReturn404_whenNotFound() throws Exception {
        String payload = """
            {"name":"No Match","email":"nomatch@example.com","password":"secret12"}
        """;

        mockMvc.perform(put("/api/users/99999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found"))
            .andExpect(jsonPath("$.path").value("/api/users/99999"))
            .andExpect(jsonPath("$.method").value("PUT"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateUser_shouldReturn400_whenMissingName() throws Exception {
        String payload = """
            {
              "name": null,
              "email": "ok@example.com",
              "password": "pw"
            }
        """;

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldReturn400_whenMissingEmail() throws Exception {
        String payload = """
            {
              "name": "Valid",
              "password": "pw"
            }
        """;

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldReturn400_whenMissingPassword() throws Exception {
        String payload = """
            {
              "name": "Valid",
              "email": "valid@example.com"
            }
        """;

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_shouldSucceed_withExistingId() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getUserId())).isFalse();
    }

    @Test
    void deleteUser_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
