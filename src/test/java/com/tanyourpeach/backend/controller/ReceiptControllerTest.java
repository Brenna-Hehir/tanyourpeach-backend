package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import com.tanyourpeach.backend.service.ReceiptService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceiptControllerTest {

    @Mock
    private ReceiptService receiptService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReceiptController controller;

    private final String token = "Bearer test-token";
    private final String email = "user@example.com";

    private User adminUser;
    private User regularUser;
    private Receipt testReceipt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setEmail(email);
        adminUser.setIsAdmin(true);

        regularUser = new User();
        regularUser.setEmail(email);
        regularUser.setIsAdmin(false);

        Appointment appt = new Appointment();
        appt.setClientEmail(email);

        testReceipt = new Receipt();
        testReceipt.setReceiptId(1L);
        testReceipt.setAppointment(appt);

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("test-token")).thenReturn(email);
    }

    @Test
    void getAllReceipts_shouldReturnReceipts_whenAdmin() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(receiptService.getAllReceipts()).thenReturn(List.of(testReceipt));

        ResponseEntity<?> response = controller.getAllReceipts(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllReceipts_shouldReturn403_whenNotAdmin() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));

        ResponseEntity<?> response = controller.getAllReceipts(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getReceiptById_shouldReturnReceipt_whenAdminAndExists() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(receiptService.getReceiptById(1L)).thenReturn(Optional.of(testReceipt));

        ResponseEntity<?> response = controller.getReceiptById(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getReceiptById_shouldReturn404_whenNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(receiptService.getReceiptById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getReceiptById(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getReceiptById_shouldReturn403_whenNotAdmin() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));

        ResponseEntity<?> response = controller.getReceiptById(1L, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getReceiptByAppointmentId_shouldReturnReceipt_whenOwner() {
        when(receiptService.getReceiptByAppointmentId(1L)).thenReturn(Optional.of(testReceipt));

        ResponseEntity<?> response = controller.getReceiptByAppointmentId(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getReceiptByAppointmentId_shouldReturnReceipt_whenAdminAccessingOtherUser() {
        Appointment otherAppt = new Appointment();
        otherAppt.setClientEmail("someoneelse@example.com");
        testReceipt.setAppointment(otherAppt);

        when(receiptService.getReceiptByAppointmentId(1L)).thenReturn(Optional.of(testReceipt));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        ResponseEntity<?> response = controller.getReceiptByAppointmentId(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getReceiptByAppointmentId_shouldReturn403_whenNotOwnerOrAdmin() {
        Appointment otherAppt = new Appointment();
        otherAppt.setClientEmail("other@example.com");
        testReceipt.setAppointment(otherAppt);

        when(receiptService.getReceiptByAppointmentId(1L)).thenReturn(Optional.of(testReceipt));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(regularUser));

        ResponseEntity<?> response = controller.getReceiptByAppointmentId(1L, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getReceiptByAppointmentId_shouldReturn404_whenNotFound() {
        when(receiptService.getReceiptByAppointmentId(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getReceiptByAppointmentId(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getReceiptByAppointmentId_shouldReturn401_whenTokenMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<?> response = controller.getReceiptByAppointmentId(1L, request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}