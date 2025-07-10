package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.AdminDashboardSummary;
import com.tanyourpeach.backend.dto.MonthlyStats;
import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.repository.ReceiptRepository;
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
    private ReceiptRepository receiptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private FinancialLogRepository financialLogRepository;

    public AdminDashboardSummary getDashboardSummary() {
        AdminDashboardSummary summary = new AdminDashboardSummary();

        BigDecimal revenue = financialLogRepository.sumByType("revenue");
        BigDecimal expenses = financialLogRepository.sumByType("expense");
        BigDecimal profit = (revenue != null ? revenue : BigDecimal.ZERO)
                .subtract(expenses != null ? expenses : BigDecimal.ZERO);

        summary.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        summary.setTotalExpenses(expenses != null ? expenses : BigDecimal.ZERO);
        summary.setTotalProfit(profit);

        return summary;
    }

    public List<MonthlyStats> getLastFourMonthsStats() {
        List<MonthlyStats> monthlyStatsList = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = 1; i <= 4; i++) {
            YearMonth month = currentMonth.minusMonths(i);
            String monthStr = String.format("%02d-%d", month.getMonthValue(), month.getYear()); // MM-YYYY

            // Still pass the original format ("YYYY-MM") to the DB query
            String dbFormat = month.toString(); // "YYYY-MM"

            BigDecimal revenue = financialLogRepository.sumByTypeAndMonth("revenue", dbFormat);
            BigDecimal expenses = financialLogRepository.sumByTypeAndMonth("expense", dbFormat);
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

    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository.findByAppointmentDateAfterOrderByAppointmentDateAsc(LocalDateTime.now());
    }

    public List<Inventory> getLowStockInventory() {
        return inventoryRepository.findItemsBelowThreshold();
    }
}