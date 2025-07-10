package com.tanyourpeach.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tanyourpeach.backend.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
        List<Appointment> findByAppointmentDateAfterOrderByAppointmentDateAsc(LocalDateTime date);
}
