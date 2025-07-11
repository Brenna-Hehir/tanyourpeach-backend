package com.tanyourpeach.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.service.AppointmentService;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.repository.AppointmentRepository;

import jakarta.servlet.http.HttpServletRequest;
import com.tanyourpeach.backend.service.JwtService;

import java.util.List;
import java.util.Optional;
import java.util.Map;

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

    @Autowired
    private AppointmentRepository appointmentRepository;

    // GET all appointments
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    // GET appointment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET appointments for the authenticated user
    @GetMapping("/my")
    public ResponseEntity<List<Appointment>> getMyAppointments(HttpServletRequest request) {
        String token = jwtService.extractToken(request); // helper to get token from header
        String email = jwtService.extractUsername(token);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Appointment> userAppointments = appointmentRepository.findByUser_UserIdOrderByAppointmentDateTimeDesc(userOpt.get().getUserId());
        return ResponseEntity.ok(userAppointments);
    }
   
    // POST new appointment
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(
        @RequestBody Appointment appointment,
        HttpServletRequest request
    ) {
        return appointmentService.createAppointment(appointment, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // PUT update appointment
    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long id,
            @RequestBody Appointment updated,
            HttpServletRequest request
    ) {
        return appointmentService.updateAppointment(id, updated, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // GET booking source stats (guest vs registered)
    @GetMapping("/admin/booking-source-stats")
    public ResponseEntity<Map<String, Long>> getBookingSourceStats() {
        return ResponseEntity.ok(appointmentService.getGuestVsRegisteredStats());
    }

    // DELETE appointment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        return appointmentService.deleteAppointment(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}