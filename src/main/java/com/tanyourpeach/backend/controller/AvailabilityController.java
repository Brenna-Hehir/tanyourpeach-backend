package com.tanyourpeach.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.service.AvailabilityService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availabilities")
@CrossOrigin(origins = "*")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    // GET all
    @GetMapping
    public List<Availability> getAllAvailabilities() {
        return availabilityService.getAllAvailabilities();
    }

    // GET available by date
    @GetMapping("/available/{date}")
    public ResponseEntity<?> getAvailableSlotsByDate(@PathVariable String date) {
        try {
            List<Availability> slots = availabilityService.getAvailableSlotsByDate(LocalDate.parse(date));
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format");
        }
    }

    // POST create new slot
    @PostMapping
    public ResponseEntity<Availability> createAvailability(@Valid @RequestBody Availability availability) {
        Availability saved = availabilityService.createAvailability(availability);
        if (saved == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(saved);
    }

    // PUT update slot
    @PutMapping("/{id}")
    public ResponseEntity<Availability> updateAvailability(@PathVariable Long id, @Valid @RequestBody Availability updated) {
        return availabilityService.updateAvailability(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // DELETE a slot
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        return availabilityService.deleteAvailability(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}