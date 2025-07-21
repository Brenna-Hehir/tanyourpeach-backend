package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.AuthenticationRequest;
import com.tanyourpeach.backend.dto.AuthenticationResponse;
import com.tanyourpeach.backend.dto.RegisterRequest;
import com.tanyourpeach.backend.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // You can customize origins if needed
public class AuthController {

    @Autowired
    private UserAuthService userAuthService;

    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(userAuthService.register(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(userAuthService.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}