package com.tanyourpeach.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.repository.AvailabilityRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    // GET all availability slots
    public List<Availability> getAllAvailabilities() {
        return availabilityRepository.findAll();
    }

    // GET availability slot by ID
    public List<Availability> getAvailableSlotsByDate(LocalDate date) {
        return availabilityRepository.findByIsBookedFalseAndDate(date);
    }

    // POST create new availability slot
    public Availability createAvailability(Availability availability) {
        // Prevent past dates
        if (availability.getDate() != null && availability.getDate().isBefore(LocalDate.now())) {
            return null; // Or throw an exception depending on your preference
        }

        // Prevent end time <= start time
        if (availability.getEndTime().isBefore(availability.getStartTime()) ||
            availability.getEndTime().equals(availability.getStartTime())) {
            return null; // or throw IllegalArgumentException if you prefer
        }

        // Prevent overlap
        List<Availability> overlaps = availabilityRepository.findByDate(availability.getDate()).stream()
            .filter(existing ->
                !(availability.getEndTime().isBefore(existing.getStartTime()) ||
                availability.getStartTime().isAfter(existing.getEndTime()))
            ).toList();

        if (!overlaps.isEmpty()) {
            return null;
        }

        return availabilityRepository.save(availability);
    }

    // PUT update availability slot
    public Optional<Availability> updateAvailability(Long id, Availability updated) {
        // Skip self in overlap check
        List<Availability> overlaps = availabilityRepository.findByDate(updated.getDate()).stream()
            .filter(existing -> !existing.getSlotId().equals(id))
            .filter(existing ->
                !(updated.getEndTime().isBefore(existing.getStartTime()) ||
                updated.getStartTime().isAfter(existing.getEndTime()))
            ).toList();

        if (!overlaps.isEmpty() ||
            updated.getEndTime().isBefore(updated.getStartTime()) ||
            updated.getEndTime().equals(updated.getStartTime())) {
            return Optional.empty();
        }

        Optional<Availability> existingOpt = availabilityRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        // Prevent past dates
        if (updated.getDate() != null && updated.getDate().isBefore(LocalDate.now())) {
            return Optional.empty();
        }

        Availability existing = existingOpt.get();
        existing.setDate(updated.getDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setIsBooked(updated.getIsBooked());
        existing.setNotes(updated.getNotes());
        existing.setAppointment(updated.getAppointment());

        return Optional.of(availabilityRepository.save(existing));
    }

    // DELETE availability slot
    public boolean deleteAvailability(Long id) {
        if (!availabilityRepository.existsById(id)) return false;
        availabilityRepository.deleteById(id);
        return true;
    }
}