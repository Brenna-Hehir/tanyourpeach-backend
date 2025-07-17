package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FinancialLogService {

    @Autowired
    private FinancialLogRepository financialLogRepository;

    // Get all logs
    public List<FinancialLog> getAllLogs() {
        return financialLogRepository.findAll();
    }

    // Get log by ID
    public Optional<FinancialLog> getLogById(Long id) {
        return financialLogRepository.findById(id);
    }

    // Create a new log
    public FinancialLog createLog(FinancialLog log) {
        // Basic validation
        if (log.getAmount() == null || log.getAmount().signum() < 0 ||
            log.getType() == null) {
            return null; // or throw IllegalArgumentException if preferred
        }

        return financialLogRepository.save(log);
    }

    // Update an existing log
    public Optional<FinancialLog> updateLog(Long id, FinancialLog updated) {
        // Basic validation
        if (updated.getAmount() == null || updated.getAmount().signum() < 0 ||
            updated.getType() == null) {
            return Optional.empty(); // prevent update if invalid
        }

        return financialLogRepository.findById(id).map(existing -> {
            existing.setType(updated.getType());
            existing.setSource(updated.getSource());
            existing.setReferenceId(updated.getReferenceId());
            existing.setDescription(updated.getDescription());
            existing.setAmount(updated.getAmount());
            return financialLogRepository.save(existing);
        });
    }

    // Delete a log
    public boolean deleteLog(Long id) {
        if (!financialLogRepository.existsById(id)) {
            return false;
        }
        financialLogRepository.deleteById(id);
        return true;
    }
}