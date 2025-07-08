package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.AuthenticationRequest;
import com.tanyourpeach.backend.dto.AuthenticationResponse;
import com.tanyourpeach.backend.dto.RegisterRequest;
import com.tanyourpeach.backend.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // You can customize origins if needed
public class AuthController {

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userAuthService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(userAuthService.authenticate(request));
    }
}