package com.tanurpeach.backend.service;

import com.tanurpeach.backend.model.Availability;
import com.tanurpeach.backend.repository.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return availabilityRepository.save(availability);
    }

    // PUT update availability slot
    public Optional<Availability> updateAvailability(Long id, Availability updated) {
        Optional<Availability> existingOpt = availabilityRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

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