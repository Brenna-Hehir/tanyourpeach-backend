package com.tanurpeach.backend.repository;

import com.tanurpeach.backend.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDate(LocalDate date);
    List<Availability> findByIsBookedFalseAndDate(LocalDate date);
}
