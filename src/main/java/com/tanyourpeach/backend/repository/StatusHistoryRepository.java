package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Long> {
}