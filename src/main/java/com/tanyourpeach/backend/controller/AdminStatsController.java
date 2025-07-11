package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.service.AdminStatsService;
import com.tanyourpeach.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin
public class AdminStatsController {

    @Autowired
    private AdminStatsService adminStatsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    // This method checks if the user is an admin based on the JWT token in the request header.
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

    // Endpoints for admin statistics
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(adminStatsService.getDashboardSummary());
    }

    // Endpoint to get the last four months of statistics
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyStats(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(adminStatsService.getLastFourMonthsStats());
    }

    // Endpoint to get the last four weeks of statistics
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingAppointments(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(adminStatsService.getUpcomingAppointments());
    }

    // Endpoint to get low stock items
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockItems(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(adminStatsService.getLowStockInventory());
    }
}