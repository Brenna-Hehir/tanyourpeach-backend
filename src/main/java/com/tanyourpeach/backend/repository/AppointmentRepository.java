package com.tanyourpeach.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tanyourpeach.backend.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}