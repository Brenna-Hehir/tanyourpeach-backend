package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.FinancialLogService;
import com.tanyourpeach.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinancialLogControllerTest {

    @Mock
    private FinancialLogService financialLogService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FinancialLogController controller;

    private User adminUser;
    private FinancialLog testLog;
    private final String token = "Bearer mock-token";
    private final String email = "admin@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setEmail(email);
        adminUser.setIsAdmin(true);

        testLog = new FinancialLog();
        testLog.setLogId(1L);
        testLog.setAmount(BigDecimal.valueOf(100.0));
    }

    @Test
    void getAllLogs_shouldReturnLogs_ifAdmin() {
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(financialLogService.getAllLogs()).thenReturn(List.of(testLog));

        ResponseEntity<?> response = controller.getAllLogs(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllLogs_shouldReturn403_ifNotAdmin() {
        User nonAdmin = new User();
        nonAdmin.setEmail(email);
        nonAdmin.setIsAdmin(false);

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(nonAdmin));

        ResponseEntity<?> response = controller.getAllLogs(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getLogById_shouldReturnLog_ifAdmin() {
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(financialLogService.getLogById(1L)).thenReturn(Optional.of(testLog));

        ResponseEntity<?> response = controller.getLogById(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testLog, response.getBody());
    }

    @Test
    void getLogById_shouldReturn404_ifNotFound() {
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(financialLogService.getLogById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getLogById(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getLogById_shouldReturn403_ifNotAdmin() {
        User nonAdmin = new User();
        nonAdmin.setEmail(email);
        nonAdmin.setIsAdmin(false);

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(nonAdmin));

        ResponseEntity<?> response = controller.getLogById(1L, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllLogs_shouldReturn403_whenAuthorizationHeaderMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);
        ResponseEntity<?> response = controller.getAllLogs(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllLogs_shouldReturn403_whenJwtThrowsException() {
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenThrow(new RuntimeException("Bad token"));
        ResponseEntity<?> response = controller.getAllLogs(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllLogs_shouldReturn403_whenUserNotFound() {
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        ResponseEntity<?> response = controller.getAllLogs(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}