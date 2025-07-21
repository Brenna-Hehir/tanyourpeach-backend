package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.AdminDashboardSummary;
import com.tanyourpeach.backend.dto.MonthlyStats;
import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.service.AdminStatsService;
import com.tanyourpeach.backend.service.JwtService;
import com.tanyourpeach.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminStatsControllerTest {

    @Mock
    private AdminStatsService adminStatsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminStatsController controller;

    private final String jwtToken = "Bearer mocktoken";
    private final String email = "admin@example.com";
    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUser = new User();
        adminUser.setEmail(email);
        adminUser.setIsAdmin(true);
    }

    @Test
    void getSummary_shouldReturn200_ifAdmin() {
        AdminDashboardSummary summaryMock = mock(AdminDashboardSummary.class);

        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(adminStatsService.getDashboardSummary()).thenReturn(summaryMock);

        ResponseEntity<?> response = controller.getSummary(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summaryMock, response.getBody());
    }

    @Test
    void getMonthlyStats_shouldReturn200_ifAdmin() {
        List<MonthlyStats> monthlyMock = List.of(mock(MonthlyStats.class));

        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(adminStatsService.getLastFourMonthsStats()).thenReturn(monthlyMock);

        ResponseEntity<?> response = controller.getMonthlyStats(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(monthlyMock, response.getBody());
    }

    @Test
    void getUpcomingAppointments_shouldReturn200_ifAdmin() {
        List<Appointment> upcomingMock = List.of(mock(Appointment.class));

        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(adminStatsService.getUpcomingAppointments()).thenReturn(upcomingMock);

        ResponseEntity<?> response = controller.getUpcomingAppointments(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(upcomingMock, response.getBody());
    }

    @Test
    void getLowStockItems_shouldReturn200_ifAdmin() {
        List<Inventory> lowStockMock = List.of(mock(Inventory.class));

        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(adminStatsService.getLowStockInventory()).thenReturn(lowStockMock);

        ResponseEntity<?> response = controller.getLowStockItems(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lowStockMock, response.getBody());
    }

    @Test
    void getSummary_shouldReturn403_ifNotAdmin() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User())); // not admin

        ResponseEntity<?> response = controller.getSummary(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getMonthlyStats_shouldReturn403_ifNotAdmin() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User())); // not admin

        ResponseEntity<?> response = controller.getMonthlyStats(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getSummary_shouldReturn403_ifTokenMissing() {
        when(request.getHeader("Authorization")).thenReturn(null); // no token

        ResponseEntity<?> response = controller.getSummary(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getSummary_shouldReturn403_ifTokenMalformed() {
        when(request.getHeader("Authorization")).thenReturn("badformat"); // not "Bearer ..."

        ResponseEntity<?> response = controller.getSummary(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}