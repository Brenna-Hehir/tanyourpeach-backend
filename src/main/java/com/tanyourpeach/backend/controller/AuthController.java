package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.AuthenticationRequest;
import com.tanyourpeach.backend.dto.AuthenticationResponse;
import com.tanyourpeach.backend.dto.RegisterRequest;
import com.tanyourpeach.backend.service.UserAuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserAuthService userAuthService;

    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userAuthService.register(request));
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(userAuthService.authenticate(request));
    }
}