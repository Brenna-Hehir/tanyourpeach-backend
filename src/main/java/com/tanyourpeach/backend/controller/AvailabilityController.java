package com.tanyourpeach.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.service.AvailabilityService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availabilities")
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format");
        }
    }

    // POST create new slot
    @PostMapping
    public ResponseEntity<Availability> createAvailability(@Valid @RequestBody Availability availability) {
        Availability saved = availabilityService.createAvailability(availability);
        if (saved == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create availability");
        }
        return ResponseEntity.ok(saved);
    }

    // PUT update slot
    @PutMapping("/{id}")
    public ResponseEntity<Availability> updateAvailability(@PathVariable Long id, @Valid @RequestBody Availability updated) {
        return availabilityService.updateAvailability(id, updated)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unable to update availability"
                ));
    }

    // DELETE a slot
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        boolean deleted = availabilityService.deleteAvailability(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability not found");
        }
        return ResponseEntity.noContent().build();
    }
}