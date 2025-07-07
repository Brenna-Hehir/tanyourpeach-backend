package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Receipt findByAppointment_AppointmentId(Long appointmentId);
}