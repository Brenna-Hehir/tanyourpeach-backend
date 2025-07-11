package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.FinancialLogService;
import com.tanyourpeach.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/financial-log")
@CrossOrigin(origins = "*")
public class FinancialLogController {

    @Autowired
    private FinancialLogService financialLogService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    // Helper method to check if the user is an admin
    private boolean isAdmin(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email).orElseThrow();
            return user.getIsAdmin() != null && user.getIsAdmin();
        } catch (Exception e) {
            return false;
        }
    }

    // Endpoint to get all financial logs (admin only)
    @GetMapping
    public ResponseEntity<?> getAllLogs(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(financialLogService.getAllLogs());
    }

    // Endpoint to create a new financial log entry
    @GetMapping("/{id}")
    public ResponseEntity<?> getLogById(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<FinancialLog> log = financialLogService.getLogById(id);
        return log.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}