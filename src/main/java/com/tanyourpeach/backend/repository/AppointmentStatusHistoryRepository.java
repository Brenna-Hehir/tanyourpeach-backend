package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Long> {

    void deleteAllByAppointment_AppointmentId(Long appointmentId);
}