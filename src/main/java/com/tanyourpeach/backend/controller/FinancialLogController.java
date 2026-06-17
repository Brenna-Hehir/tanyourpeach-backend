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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/financial-log")
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
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(financialLogService.getAllLogs());
    }

    // Endpoint to create a new financial log entry
    @GetMapping("/{id}")
    public ResponseEntity<?> getLogById(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new AccessDeniedException("Access denied");
        }

        Optional<FinancialLog> log = financialLogService.getLogById(id);
        return log.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Financial log not found"
                ));
    }
}