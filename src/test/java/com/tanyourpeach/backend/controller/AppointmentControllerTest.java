package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.AppointmentService;
import com.tanyourpeach.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AppointmentController controller;

    private final String jwtToken = "Bearer mocktoken";
    private final String email = "user@example.com";
    private final String adminEmail = "admin@example.com";
    private User adminUser;
    private User normalUser;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setIsAdmin(true);

        normalUser = new User();
        normalUser.setEmail(email);
        normalUser.setIsAdmin(false);

        testAppointment = new Appointment();
        testAppointment.setAppointmentId(1L);
        testAppointment.setClientEmail(email);
    }

    @Test
    void getAllAppointments_shouldReturn200_ifAdmin() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(adminEmail);
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(appointmentService.getAllAppointments()).thenReturn(List.of(testAppointment));

        ResponseEntity<?> response = controller.getAllAppointments(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllAppointments_shouldReturn403_ifNotAdmin() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));

        ResponseEntity<?> response = controller.getAllAppointments(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllAppointments_shouldReturn403_ifTokenMalformed() {
        when(request.getHeader("Authorization")).thenReturn("BadTokenFormat");

        ResponseEntity<?> response = controller.getAllAppointments(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllAppointments_shouldReturn403_ifUserIsAdminNull() {
        User nullAdmin = new User();
        nullAdmin.setEmail(adminEmail);
        nullAdmin.setIsAdmin(null);  // explicitly null

        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(adminEmail);
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(nullAdmin));

        ResponseEntity<?> response = controller.getAllAppointments(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAllAppointments_shouldReturn403_ifTokenInvalid() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenThrow(new RuntimeException("invalid token"));

        ResponseEntity<?> response = controller.getAllAppointments(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAppointmentById_shouldReturn200_ifOwner() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);

        ResponseEntity<?> response = controller.getAppointmentById(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAppointmentById_shouldReturn200_ifAdmin() {
        testAppointment.setClientEmail("someone@example.com");
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(adminEmail);
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

        ResponseEntity<?> response = controller.getAppointmentById(1L, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAppointmentById_shouldReturn403_ifNotOwnerOrAdmin() {
        testAppointment.setClientEmail("someone@example.com");
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));

        ResponseEntity<?> response = controller.getAppointmentById(1L, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAppointmentById_shouldReturn403_ifTokenMissing() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<?> response = controller.getAppointmentById(1L, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAppointmentById_shouldReturn404_ifNotFound() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getAppointmentById(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUserAppointments_shouldReturnAppointmentsForUser() {
        Appointment a2 = new Appointment();
        a2.setClientEmail("someone@example.com");
        when(appointmentService.getAllAppointments()).thenReturn(List.of(testAppointment, a2));
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);

        ResponseEntity<?> response = controller.getUserAppointments(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> list = (List<?>) response.getBody();
        assertEquals(1, list.size());
    }

    @Test
    void getUserAppointments_shouldReturn401_ifEmailNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        ResponseEntity<?> response = controller.getUserAppointments(request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createAppointment_shouldReturn200_ifSuccess() {
        when(appointmentService.createAppointment(any(), eq(request))).thenReturn(Optional.of(testAppointment));
        ResponseEntity<Appointment> response = controller.createAppointment(testAppointment, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createAppointment_shouldPass_ifAnonymous() {
        when(appointmentService.createAppointment(any(), eq(request))).thenReturn(Optional.of(testAppointment));

        ResponseEntity<Appointment> response = controller.createAppointment(testAppointment, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createAppointment_shouldReturn400_ifFailure() {
        when(appointmentService.createAppointment(any(), eq(request))).thenReturn(Optional.empty());
        ResponseEntity<Appointment> response = controller.createAppointment(testAppointment, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateAppointment_shouldReturn200_ifAdmin() {
        Appointment updated = new Appointment();
        updated.setClientEmail("someone@example.com");

        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(adminEmail);
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(appointmentService.updateAppointment(eq(1L), any(), eq(request))).thenReturn(Optional.of(updated));

        ResponseEntity<?> response = controller.updateAppointment(1L, updated, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateAppointment_shouldReturn403_ifNotOwnerOrAdmin() {
        testAppointment.setClientEmail("someone@example.com");

        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));

        ResponseEntity<?> response = controller.updateAppointment(1L, new Appointment(), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateAppointment_shouldReturn403_ifTokenMissing() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<?> response = controller.updateAppointment(1L, new Appointment(), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateAppointment_shouldReturn404_ifNotFound() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateAppointment(1L, new Appointment(), request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateAppointment_shouldReturn400_ifUpdateFails() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));
        when(appointmentService.updateAppointment(eq(1L), any(), eq(request))).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateAppointment(1L, new Appointment(), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateAppointment_shouldReturn403_ifUserNotAuthorized() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.of(testAppointment));
        when(request.getHeader("Authorization")).thenReturn("Bearer mocktoken");
        when(jwtService.extractUsername("mocktoken")).thenReturn("unauthorized@example.com");
        when(userRepository.findByEmail("unauthorized@example.com")).thenReturn(Optional.of(normalUser));

        ResponseEntity<?> response = controller.updateAppointment(1L, new Appointment(), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateAppointment_shouldReturn404_whenAppointmentNotFound() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Optional.empty());
        when(request.getHeader("Authorization")).thenReturn("Bearer mocktoken");

        ResponseEntity<?> response = controller.updateAppointment(1L, new Appointment(), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteAppointment_shouldReturn204_ifAdminAndDeleted() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(adminEmail);
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(appointmentService.deleteAppointment(1L)).thenReturn(true);

        ResponseEntity<?> response = controller.deleteAppointment(1L, request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteAppointment_shouldReturn403_ifNotAdmin() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));

        ResponseEntity<?> response = controller.deleteAppointment(1L, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteAppointment_shouldReturn404_ifNotFound() {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.extractUsername("mocktoken")).thenReturn(adminEmail);
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(appointmentService.deleteAppointment(1L)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteAppointment(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}