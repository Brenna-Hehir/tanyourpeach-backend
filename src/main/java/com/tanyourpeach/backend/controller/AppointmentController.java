package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.AppointmentService;
import com.tanyourpeach.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

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

    // GET all appointments (admin only)
    @GetMapping
    public ResponseEntity<?> getAllAppointments(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // GET appointment by ID (user must own it or be admin)
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
        if (appointment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String email = getUserEmail(request);
        String ownerEmail = appointment.get().getClientEmail();

        if (ownerEmail != null && ownerEmail.equals(email)) {
            return ResponseEntity.ok(appointment.get());
        }

        if (isAdmin(request)) {
            return ResponseEntity.ok(appointment.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    // GET all appointments for logged-in user
    @GetMapping("/my-appointments")
    public ResponseEntity<?> getUserAppointments(HttpServletRequest request) {
        String email = getUserEmail(request);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        List<Appointment> userAppointments = appointmentService.getAllAppointments().stream()
                .filter(a -> a.getClientEmail() != null && a.getClientEmail().equals(email))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userAppointments);
    }

    // POST create appointment (open to anonymous or logged-in)
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment, HttpServletRequest request) {
        Optional<Appointment> created = appointmentService.createAppointment(appointment, request);
        return created.map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
    }

    // PUT update appointment (admins or user that owns it)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @Valid @RequestBody Appointment updated, HttpServletRequest request) {
        Optional<Appointment> existing = appointmentService.getAppointmentById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String email = getUserEmail(request);
        String ownerEmail = existing.get().getClientEmail();

        if (isAdmin(request) || (ownerEmail != null && ownerEmail.equals(email))) {
            return appointmentService.updateAppointment(id, updated, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.badRequest().build());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    // DELETE appointment (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return appointmentService.deleteAppointment(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}