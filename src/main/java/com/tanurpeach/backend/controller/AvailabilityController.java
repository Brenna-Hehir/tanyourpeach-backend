package com.tanurpeach.backend.controller;


import com.tanurpeach.backend.model.Availability;
import com.tanurpeach.backend.repository.AvailabilityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/availabilities")
@CrossOrigin(origins = "*")
public class AvailabilityController {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    // GET all
    @GetMapping
    public List<Availability> getAllAvailabilities() {
        return availabilityRepository.findAll();
    }

    // GET available by date
    @GetMapping("/available/{date}")
    public List<Availability> getAvailableSlotsByDate(@PathVariable String date) {
        return availabilityRepository.findByIsBookedFalseAndDate(LocalDate.parse(date));
    }

    // POST create new slot
    @PostMapping
    public ResponseEntity<Availability> createAvailability(@RequestBody Availability availability) {
        Availability saved = availabilityRepository.save(availability);
        return ResponseEntity.ok(saved);
    }

    // PUT update slot (e.g., to mark booked)
    @PutMapping("/{id}")
    public ResponseEntity<Availability> updateAvailability(@PathVariable Long id, @RequestBody Availability updated) {
        Optional<Availability> existingOpt = availabilityRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Availability existing = existingOpt.get();
        existing.setDate(updated.getDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setIsBooked(updated.getIsBooked());
        existing.setNotes(updated.getNotes());
        existing.setAppointment(updated.getAppointment());

        Availability saved = availabilityRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    // DELETE a slot
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        if (!availabilityRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        availabilityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
