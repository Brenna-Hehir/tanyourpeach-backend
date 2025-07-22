package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.FinancialLog;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialLogRepository extends JpaRepository<FinancialLog, Long> {

    @Query("SELECT SUM(f.amount) FROM FinancialLog f WHERE f.type = :type")
    BigDecimal sumByType(FinancialLog.Type type);
    
    @Query("SELECT SUM(f.amount) FROM FinancialLog f " +
       "WHERE f.type = :type AND FUNCTION('DATE_FORMAT', f.logDate, '%Y-%m') = :month")
    BigDecimal sumByTypeAndMonth(FinancialLog.Type type, String month);
}