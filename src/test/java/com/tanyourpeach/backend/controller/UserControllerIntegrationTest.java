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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDataCleaner testDataCleaner;

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
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("newpass");
        newUser.setIsAdmin(true);
        newUser.setAddress("456 Other St");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void createUser_shouldReturn400_whenMissingEmail() throws Exception {
        User invalid = new User();
        invalid.setName("No Email");
        invalid.setPasswordHash("pass");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400_whenMissingPassword() throws Exception {
        User invalid = new User();
        invalid.setName("No Pass");
        invalid.setEmail("nopass@example.com");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400_whenEmailInvalidFormat() throws Exception {
        User invalid = new User();
        invalid.setName("Bad Email");
        invalid.setEmail("invalid-email");
        invalid.setPasswordHash("pass");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400_whenDuplicateEmail() throws Exception {
        User duplicate = new User();
        duplicate.setName("Duplicate");
        duplicate.setEmail("test@example.com");
        duplicate.setPasswordHash("pass");
        duplicate.setIsAdmin(false);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldSucceed_withValidUpdate() throws Exception {
        testUser.setName("Updated Name");

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateUser_shouldReturn404_whenNotFound() throws Exception {
        testUser.setName("No Match");

        mockMvc.perform(put("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldReturn400_whenMissingName() throws Exception {
        testUser.setName(null);

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldReturn400_whenMissingEmail() throws Exception {
        testUser.setEmail(null);

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldReturn400_whenMissingPassword() throws Exception {
        testUser.setPasswordHash(null);

        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
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