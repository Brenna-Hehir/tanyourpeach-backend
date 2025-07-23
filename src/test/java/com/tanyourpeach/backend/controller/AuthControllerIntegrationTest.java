package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.dto.AuthenticationRequest;
import com.tanyourpeach.backend.dto.RegisterRequest;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldSucceedWithValidInput() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Brenna");
        request.setEmail("brenna@example.com");
        request.setPassword("secure123");
        request.setAddress("123 Main St");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        assertThat(userRepository.findByEmail("brenna@example.com")).isPresent();
    }

    @Test
    void register_shouldFailWithDuplicateEmail() throws Exception {
        // Pre-save a user
        User existing = new User();
        existing.setName("Existing");
        existing.setEmail("dupe@example.com");
        existing.setPasswordHash(passwordEncoder.encode("something"));
        userRepository.save(existing);

        RegisterRequest request = new RegisterRequest();
        request.setName("New User");
        request.setEmail("dupe@example.com");
        request.setPassword("something");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void register_shouldFailWhenNameMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldFailWhenEmailMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldFailWhenPasswordMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldSucceedWithCorrectCredentials() throws Exception {
        // Pre-register user with known email/password combo
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Brenna");
        reg.setEmail("brenna@example.com");
        reg.setPassword("secure123");
        reg.setAddress("123 Main St");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        AuthenticationRequest login = new AuthenticationRequest();
        login.setEmail("brenna@example.com");
        login.setPassword("secure123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_shouldFailWithWrongPassword() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Brenna");
        reg.setEmail("brenna@example.com");
        reg.setPassword("secure123");
        reg.setAddress("123 Main St");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        AuthenticationRequest login = new AuthenticationRequest();
        login.setEmail("brenna@example.com");
        login.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_shouldFailForNonexistentUser() throws Exception {
        AuthenticationRequest login = new AuthenticationRequest();
        login.setEmail("ghost@example.com");
        login.setPassword("anything");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_shouldFailWhenEmailMissing() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldFailWhenPasswordMissing() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}