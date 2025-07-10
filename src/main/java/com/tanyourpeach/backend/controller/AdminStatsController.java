package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.AdminDashboardSummary;
import com.tanyourpeach.backend.dto.MonthlyStats;
import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.service.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin
public class AdminStatsController {

    @Autowired
    private AdminStatsService adminStatsService;

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummary> getSummary() {
        return ResponseEntity.ok(adminStatsService.getDashboardSummary());
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyStats>> getMonthlyStats() {
        return ResponseEntity.ok(adminStatsService.getLastFourMonthsStats());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Appointment>> getUpcomingAppointments() {
        return ResponseEntity.ok(adminStatsService.getUpcomingAppointments());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        return ResponseEntity.ok(adminStatsService.getLowStockInventory());
    }
}