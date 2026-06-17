package com.tanyourpeach.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tanyourpeach.backend.model.Availability;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Availability a where a.slotId = :slotId")
    Optional<Availability> findBySlotIdForUpdate(@Param("slotId") Long slotId);

    List<Availability> findByDate(LocalDate date);

    List<Availability> findByIsBookedFalseAndDate(LocalDate date);
}