package com.tanyourpeach.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tanyourpeach.backend.model.Availability;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDate(LocalDate date);
    List<Availability> findByIsBookedFalseAndDate(LocalDate date);
}
