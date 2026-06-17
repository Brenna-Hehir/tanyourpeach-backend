package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import com.tanyourpeach.backend.service.ReceiptService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

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

    // Helper method to get the email of the user from the JWT token
    private String getUserEmail(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            return jwtService.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }

    // GET all receipts (admin only)
    @GetMapping
    public ResponseEntity<?> getAllReceipts(HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(receiptService.getAllReceipts());
    }

    // GET receipt by receipt ID (admin only)
    @GetMapping("/{id}")
    public ResponseEntity<?> getReceiptById(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new AccessDeniedException("Access denied");
        }

        Optional<Receipt> receipt = receiptService.getReceiptById(id);
        return receipt.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Receipt not found"
                ));
    }

    // GET receipt by appointment ID (user can only fetch their own)
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getReceiptByAppointmentId(@PathVariable Long appointmentId, HttpServletRequest request) {
        String email = getUserEmail(request);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Receipt receipt = receiptService.getReceiptByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Receipt not found"
                ));

        String receiptOwner = receipt.getAppointment().getClientEmail();
        if (receiptOwner != null && receiptOwner.equals(email)) {
            return ResponseEntity.ok(receipt);
        }

        // Only admins can access someone else's receipt
        if (isAdmin(request)) {
            return ResponseEntity.ok(receipt);
        }

        throw new AccessDeniedException("Access denied");
    }
}