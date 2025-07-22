package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.AdminDashboardSummary;
import com.tanyourpeach.backend.dto.MonthlyStats;
import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminStatsService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private FinancialLogRepository financialLogRepository;

    // Retrieves the summary for the admin dashboard
    public AdminDashboardSummary getDashboardSummary() {
        AdminDashboardSummary summary = new AdminDashboardSummary();

        BigDecimal revenue = financialLogRepository.sumByType(FinancialLog.Type.revenue);
        BigDecimal expenses = financialLogRepository.sumByType(FinancialLog.Type.expense);
        BigDecimal profit = (revenue != null ? revenue : BigDecimal.ZERO)
                .subtract(expenses != null ? expenses : BigDecimal.ZERO);

        summary.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        summary.setTotalExpenses(expenses != null ? expenses : BigDecimal.ZERO);
        summary.setTotalProfit(profit);

        return summary;
    }

    // Retrieves the last four months' financial stats
    public List<MonthlyStats> getLastFourMonthsStats() {
        List<MonthlyStats> monthlyStatsList = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        // Loop through the last 4 months
        for (int i = 1; i <= 4; i++) {
            YearMonth month = currentMonth.minusMonths(i);
            String monthStr = String.format("%02d-%d", month.getMonthValue(), month.getYear()); // MM-YYYY

            // Still pass the original format ("YYYY-MM") to the DB query
            String dbFormat = month.toString(); // "YYYY-MM"

            BigDecimal revenue = financialLogRepository.sumByTypeAndMonth(FinancialLog.Type.revenue, dbFormat);
            BigDecimal expenses = financialLogRepository.sumByTypeAndMonth(FinancialLog.Type.expense, dbFormat);
            BigDecimal profit = (revenue != null ? revenue : BigDecimal.ZERO)
                    .subtract(expenses != null ? expenses : BigDecimal.ZERO);

            MonthlyStats stats = new MonthlyStats();
            stats.setMonth(monthStr); // formatted for frontend
            stats.setRevenue(revenue != null ? revenue : BigDecimal.ZERO);
            stats.setExpenses(expenses != null ? expenses : BigDecimal.ZERO);
            stats.setProfit(profit);

            monthlyStatsList.add(stats);
        }

        return monthlyStatsList;
    }

    // Retrieves upcoming appointments
    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository.findByAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(LocalDateTime.now());
    }

    // Retrieves inventory items that are below the low stock threshold
    public List<Inventory> getLowStockInventory() {
        return inventoryRepository.findItemsBelowThreshold();
    }
}