package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.service.FinancialLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/financial-log")
@CrossOrigin(origins = "*")
public class FinancialLogController {

    @Autowired
    private FinancialLogService financialLogService;

    // GET all logs
    @GetMapping
    public List<FinancialLog> getAllLogs() {
        return financialLogService.getAllLogs();
    }

    // GET single log by ID
    @GetMapping("/{id}")
    public ResponseEntity<FinancialLog> getLogById(@PathVariable Long id) {
        Optional<FinancialLog> log = financialLogService.getLogById(id);
        return log.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // POST create a new log entry
    @PostMapping
    public FinancialLog createLog(@RequestBody FinancialLog log) {
        return financialLogService.createLog(log);
    }

    // PUT update an existing log
    @PutMapping("/{id}")
    public ResponseEntity<FinancialLog> updateLog(@PathVariable Long id, @RequestBody FinancialLog updated) {
        return financialLogService.updateLog(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE log by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        return financialLogService.deleteLog(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}