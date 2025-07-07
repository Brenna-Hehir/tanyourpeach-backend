package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.FinancialLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialLogRepository extends JpaRepository<FinancialLog, Long> {
}