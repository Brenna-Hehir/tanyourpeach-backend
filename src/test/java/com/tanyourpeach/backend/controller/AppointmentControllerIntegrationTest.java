package com.tanyourpeach.backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.model.*;
import com.tanyourpeach.backend.repository.*;
import com.tanyourpeach.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppointmentControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private AvailabilityRepository availabilityRepository;


    @Autowired
    private TanServiceRepository tanServiceRepository;


    @Autowired
    private JwtService jwtService;


    private String adminToken;
    private String userToken;
    private User admin;
    private User user;
    private TanService service;
    private Availability availability;
    private Appointment appointment;


    @BeforeEach
    void setup() {
        appointmentStatusHistoryRepository.deleteAll();
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        tanServiceRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("admin");
        admin.setIsAdmin(true);
        userRepository.save(admin);
        adminToken = "Bearer " + jwtService.generateToken(admin);

        user = new User();
        user.setName("User");
        user.setEmail("user@example.com");
        user.setPasswordHash("user");
        user.setIsAdmin(false);
        userRepository.save(user);
        userToken = "Bearer " + jwtService.generateToken(user);

        service = new TanService();
        service.setName("Glow Up");
        service.setBasePrice(50.0);
        service.setDurationMinutes(30);
        service.setIsActive(true);
        tanServiceRepository.save(service);

        availability = new Availability();
        availability.setDate(LocalDate.now().plusDays(1));
        availability.setStartTime(LocalTime.of(14, 0));
        availability.setEndTime(LocalTime.of(14, 30));
        availability.setIsBooked(false);
        availability = availabilityRepository.save(availability); // Save first without linking

        appointment = new Appointment();
        appointment.setService(service);
        appointment.setClientName("Brenna");
        appointment.setClientEmail(user.getEmail());
        appointment.setClientAddress("123 Peach St");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        appointment.setAvailability(availability);
        appointment = appointmentRepository.save(appointment);

        // Save appointment first, without linking the availability
        appointment.setAvailability(availability);
        appointment = appointmentRepository.save(appointment);

        availabilityRepository.save(availability);
    }


    // ---------- GET /api/appointments (admin only) ----------


    @Test
    void getAllAppointments_shouldReturnForAdmin() throws Exception {
        mockMvc.perform(get("/api/appointments")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }


    @Test
    void getAllAppointments_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/appointments")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }


    @Test
    void getAllAppointments_shouldReturnForbiddenIfNoToken() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isForbidden());
    }


    // ---------- GET /api/appointments/{id} ----------


    @Test
    void getAppointmentById_shouldReturnForAdmin() throws Exception {
        mockMvc.perform(get("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }


    @Test
    void getAppointmentById_shouldReturnForOwner() throws Exception {
        mockMvc.perform(get("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", userToken))
                .andExpect(status().isOk());
    }


    @Test
    void getAppointmentById_shouldReturnForbiddenForOtherUser() throws Exception {
        User other = new User();
        other.setName("Other");
        other.setEmail("other@example.com");
        other.setPasswordHash("pass");
        other.setIsAdmin(false);
        userRepository.save(other);


        String otherToken = "Bearer " + jwtService.generateToken(other);


        mockMvc.perform(get("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", otherToken))
                .andExpect(status().isForbidden());
    }


    @Test
    void getAppointmentById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/appointments/999999")
                .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }


    // ---------- GET /api/appointments/my-appointments ----------


    @Test
    void getUserAppointments_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/appointments/my-appointments")
                .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }


    @Test
    void getUserAppointments_shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/appointments/my-appointments"))
                .andExpect(status().isUnauthorized());
    }


    // ---------- POST /api/appointments ----------


    @Test
    void createAppointment_shouldSucceedWithValidData() throws Exception {
        Availability newAvailability = new Availability();
        newAvailability.setDate(LocalDate.now().plusDays(2));
        newAvailability.setStartTime(LocalTime.of(15, 0));
        newAvailability.setEndTime(LocalTime.of(15, 30));
        newAvailability.setIsBooked(false);
        availabilityRepository.save(newAvailability);


        Appointment newAppt = new Appointment();
        newAppt.setService(service);
        newAppt.setClientName("New Client");
        newAppt.setClientEmail("newclient@example.com");
        newAppt.setClientAddress("456 New Rd");
        newAppt.setAppointmentDateTime(LocalDateTime.now().plusDays(3));
        newAppt.setAvailability(newAvailability);


        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAppt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("New Client"));


        assertThat(appointmentRepository.findAll())
                .anyMatch(a -> a.getClientEmail().equals("newclient@example.com"));
    }


    @Test
    void createAppointment_shouldFailWithMissingName() throws Exception {
        Appointment invalid = new Appointment();
        invalid.setService(service);
        invalid.setClientEmail("fail@example.com");
        invalid.setClientAddress("fail");
        invalid.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        invalid.setAvailability(availability);


        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }


    // ---------- PUT /api/appointments/{id} ----------


    @Test
    void updateAppointment_shouldSucceedForAdmin() throws Exception {
        appointment.setClientName("Updated Admin");


        mockMvc.perform(put("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Updated Admin"));
    }


    @Test
    void updateAppointment_shouldSucceedForOwner() throws Exception {
        appointment.setClientName("Updated Owner");


        mockMvc.perform(put("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Updated Owner"));
    }


    @Test
    void updateAppointment_shouldFailForNonOwner() throws Exception {
        User other = new User();
        other.setName("Other");
        other.setEmail("other@example.com");
        other.setPasswordHash("pass");
        other.setIsAdmin(false);
        userRepository.save(other);
        String otherToken = "Bearer " + jwtService.generateToken(other);


        mockMvc.perform(put("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
                .andExpect(status().isForbidden());
    }


    @Test
    void updateAppointment_shouldReturnNotFoundForInvalidId() throws Exception {
        appointment.setClientName("New Name");


        mockMvc.perform(put("/api/appointments/999999")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appointment)))
                .andExpect(status().isNotFound());
    }


    // ---------- DELETE /api/appointments/{id} ----------


    @Test
    void deleteAppointment_shouldSucceedForAdmin() throws Exception {
        mockMvc.perform(delete("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", adminToken))
                .andExpect(status().isNoContent());


        assertThat(appointmentRepository.findById(appointment.getAppointmentId())).isEmpty();
    }


    @Test
    void deleteAppointment_shouldReturnForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/appointments/" + appointment.getAppointmentId())
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }


    @Test
    void deleteAppointment_shouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(delete("/api/appointments/999999")
                .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }
}