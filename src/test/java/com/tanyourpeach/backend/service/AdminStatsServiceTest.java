package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.AdminDashboardSummary;
import com.tanyourpeach.backend.dto.MonthlyStats;
import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import com.tanyourpeach.backend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdminStatsServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private FinancialLogRepository financialLogRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDashboardSummary_shouldReturnCorrectProfit() {
        when(financialLogRepository.sumByType("revenue")).thenReturn(BigDecimal.valueOf(500));
        when(financialLogRepository.sumByType("expense")).thenReturn(BigDecimal.valueOf(200));

        AdminDashboardSummary summary = adminStatsService.getDashboardSummary();

        assertEquals(BigDecimal.valueOf(500), summary.getTotalRevenue());
        assertEquals(BigDecimal.valueOf(200), summary.getTotalExpenses());
        assertEquals(BigDecimal.valueOf(300), summary.getTotalProfit());
    }

    @Test
    void getDashboardSummary_shouldHandleNullValuesSafely() {
        when(financialLogRepository.sumByType("revenue")).thenReturn(null);
        when(financialLogRepository.sumByType("expense")).thenReturn(null);

        AdminDashboardSummary summary = adminStatsService.getDashboardSummary();

        assertEquals(BigDecimal.ZERO, summary.getTotalRevenue());
        assertEquals(BigDecimal.ZERO, summary.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, summary.getTotalProfit());
    }

    @Test
    void getLastFourMonthsStats_shouldReturnValidStatsList() {
        when(financialLogRepository.sumByTypeAndMonth(anyString(), anyString())).thenReturn(BigDecimal.valueOf(100));

        List<MonthlyStats> stats = adminStatsService.getLastFourMonthsStats();

        assertEquals(4, stats.size());
        for (MonthlyStats stat : stats) {
            assertEquals(BigDecimal.valueOf(100), stat.getRevenue());
            assertEquals(BigDecimal.valueOf(100), stat.getExpenses());
            assertEquals(BigDecimal.ZERO, stat.getProfit()); // 100 - 100 = 0
        }
    }

    @Test
    void getUpcomingAppointments_shouldReturnList() {
        List<Appointment> mockAppointments = List.of(new Appointment(), new Appointment());
        when(appointmentRepository.findByAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(any()))
                .thenReturn(mockAppointments);

        List<Appointment> result = adminStatsService.getUpcomingAppointments();
        assertEquals(2, result.size());
    }

    @Test
    void getLowStockInventory_shouldReturnLowStockItems() {
        List<Inventory> mockItems = List.of(new Inventory());
        when(inventoryRepository.findItemsBelowThreshold()).thenReturn(mockItems);

        List<Inventory> result = adminStatsService.getLowStockInventory();
        assertEquals(1, result.size());
    }

    @Test
    void getLastFourMonthsStats_shouldHandleMixedAndNullValues() {
        when(financialLogRepository.sumByTypeAndMonth(eq("revenue"), anyString()))
            .thenReturn(BigDecimal.valueOf(150))
            .thenReturn(null)
            .thenReturn(BigDecimal.valueOf(0))
            .thenReturn(BigDecimal.valueOf(50));

        when(financialLogRepository.sumByTypeAndMonth(eq("expense"), anyString()))
            .thenReturn(BigDecimal.valueOf(100))
            .thenReturn(BigDecimal.valueOf(200))
            .thenReturn(null)
            .thenReturn(BigDecimal.valueOf(20));

        List<MonthlyStats> stats = adminStatsService.getLastFourMonthsStats();

        assertEquals(4, stats.size());
        assertEquals(BigDecimal.valueOf(50), stats.get(0).getProfit());   // 150 - 100
        assertEquals(BigDecimal.valueOf(0).subtract(BigDecimal.valueOf(200)), stats.get(1).getProfit()); // 0 - 200
        assertEquals(BigDecimal.valueOf(0), stats.get(2).getProfit());    // 0 - 0 (nulls)
        assertEquals(BigDecimal.valueOf(30), stats.get(3).getProfit());   // 50 - 20
    }

    @Test
    void getUpcomingAppointments_shouldReturnEmptyList_whenNoAppointmentsExist() {
        when(appointmentRepository.findByAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(any()))
                .thenReturn(List.of());

        List<Appointment> result = adminStatsService.getUpcomingAppointments();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLowStockInventory_shouldReturnEmptyList_whenNoLowStockItemsExist() {
        when(inventoryRepository.findItemsBelowThreshold()).thenReturn(List.of());

        List<Inventory> result = adminStatsService.getLowStockInventory();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}