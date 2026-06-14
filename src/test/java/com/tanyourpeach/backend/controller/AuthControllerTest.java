package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.AuthenticationRequest;
import com.tanyourpeach.backend.dto.AuthenticationResponse;
import com.tanyourpeach.backend.dto.RegisterRequest;
import com.tanyourpeach.backend.service.UserAuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserAuthService userAuthService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private AuthenticationRequest loginRequest;
    private AuthenticationResponse mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registerRequest = new RegisterRequest();
        registerRequest.setName("Brenna");
        registerRequest.setEmail("brenna@example.com");
        registerRequest.setPassword("secure");
        registerRequest.setAddress("Peach Street");

        loginRequest = new AuthenticationRequest();
        loginRequest.setEmail("brenna@example.com");
        loginRequest.setPassword("secure");

        mockResponse = new AuthenticationResponse("mockToken");
    }

    @Test
    void register_shouldReturn200_withToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Brenna");
        request.setEmail("brenna@example.com");
        request.setPassword("secure");
        request.setAddress("Peach Street");

        AuthenticationResponse mockResponse = new AuthenticationResponse("mockToken");
        when(userAuthService.register(request)).thenReturn(mockResponse);

        ResponseEntity<AuthenticationResponse> response = authController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mockToken", response.getBody().getToken());
    }

    @Test
    void register_shouldThrow_ifServiceThrowsException() {
        RegisterRequest request = new RegisterRequest();
        when(userAuthService.register(request)).thenThrow(new RuntimeException("something failed"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authController.register(request)
        );

        assertEquals("something failed", ex.getMessage());
    }

    @Test
    void login_shouldReturnTokenResponse() {
        when(userAuthService.authenticate(loginRequest)).thenReturn(mockResponse);

        ResponseEntity<AuthenticationResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mockToken", response.getBody().getToken());
    }

    @Test
    void login_shouldThrow_ifServiceThrowsException() {
        AuthenticationRequest request = new AuthenticationRequest();
        when(userAuthService.authenticate(request)).thenThrow(new RuntimeException("bad login"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authController.login(request)
        );

        assertEquals("bad login", ex.getMessage());
    }
}