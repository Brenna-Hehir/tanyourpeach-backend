package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Receipt findByAppointment_AppointmentId(Long appointmentId);

    @Query("SELECT SUM(r.totalAmount) FROM Receipt r")
    BigDecimal sumTotalRevenue();
}