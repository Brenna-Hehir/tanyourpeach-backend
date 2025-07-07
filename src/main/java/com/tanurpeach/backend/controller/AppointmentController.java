package com.tanurpeach.backend.controller;

import com.tanurpeach.backend.model.Appointment;
import com.tanurpeach.backend.model.Service;
import com.tanurpeach.backend.repository.AppointmentRepository;
import com.tanurpeach.backend.repository.ServiceRepository;
import com.tanurpeach.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    // GET all appointments
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // GET appointment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST new appointment
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        // Make sure the service exists
        if (appointment.getService() == null || appointment.getService().getServiceId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<Service> serviceOpt = serviceRepository.findById(appointment.getService().getServiceId());
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Optionally validate user (if logged in)
        if (appointment.getUser() != null && appointment.getUser().getUserId() != null) {
            userRepository.findById(appointment.getUser().getUserId())
                    .ifPresent(appointment::setUser);
        } else {
            appointment.setUser(null); // force null if not a valid user
        }

        // Set service and base price
        appointment.setService(serviceOpt.get());
        appointment.setBasePrice(serviceOpt.get().getBasePrice());

        // Compute total price
        double total = appointment.getBasePrice() + (appointment.getTravelFee() != null ? appointment.getTravelFee() : 0);
        appointment.setTotalPrice(total);

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }

    // PUT update appointment
    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment updated) {
        Optional<Appointment> existingOpt = appointmentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment existing = existingOpt.get();

        // Update only editable fields
        existing.setClientName(updated.getClientName());
        existing.setClientEmail(updated.getClientEmail());
        existing.setClientAddress(updated.getClientAddress());
        existing.setAppointmentDateTime(updated.getAppointmentDateTime());
        existing.setDistanceMiles(updated.getDistanceMiles());
        existing.setTravelFee(updated.getTravelFee());
        existing.setNotes(updated.getNotes());
        existing.setStatus(updated.getStatus());

        // If service changed, refetch and reassign
        if (updated.getService() != null && updated.getService().getServiceId() != null) {
            serviceRepository.findById(updated.getService().getServiceId()).ifPresent(service -> {
                existing.setService(service);
                existing.setBasePrice(service.getBasePrice());
            });
      }

        // Recalculate total
        double total = existing.getBasePrice() + (existing.getTravelFee() != null ? existing.getTravelFee() : 0);
        existing.setTotalPrice(total);

        Appointment saved = appointmentRepository.save(existing);
        return ResponseEntity.ok(saved);
    }


    // DELETE appointment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        if (!appointmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        appointmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
