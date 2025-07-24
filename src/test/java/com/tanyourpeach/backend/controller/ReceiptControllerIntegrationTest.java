package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.*;
import com.tanyourpeach.backend.repository.*;
import com.tanyourpeach.backend.service.JwtService;
import com.tanyourpeach.backend.util.TestDataCleaner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReceiptControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private TanServiceRepository tanServiceRepository;

    @Autowired
    private TestDataCleaner testDataCleaner;

    private String adminToken;

    private String userToken;

    private Receipt testReceipt;

    private Appointment userAppointment;

    @BeforeEach
    void setup() {
        testDataCleaner.cleanAll();
    
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("pass");
        admin.setIsAdmin(true);
        userRepository.save(admin);

        User user = new User();
        user.setName("User");
        user.setEmail("user@example.com");
        user.setPasswordHash("pass");
        user.setIsAdmin(false);
        userRepository.save(user);

        adminToken = "Bearer " + jwtService.generateToken(admin);
        userToken = "Bearer " + jwtService.generateToken(user);

        TanService service = new TanService();
        service.setName("Test Service");
        service.setBasePrice(100.0);
        service.setDurationMinutes(30);
        service.setIsActive(true);
        tanServiceRepository.save(service);

        Availability slot = new Availability();
        slot.setDate(LocalDate.now().plusDays(1));
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(10, 30));
        slot.setIsBooked(true);
        availabilityRepository.save(slot);

        Appointment appt = new Appointment();
        appt.setService(service);
        appt.setClientName("Jane Doe");
        appt.setClientEmail(user.getEmail());
        appt.setClientAddress("123 Test St");
        appt.setStatus(Appointment.Status.CONFIRMED);
        appt.setAppointmentDateTime(LocalDateTime.of(slot.getDate(), slot.getStartTime()));
        appt.setAvailability(slot);
        appt.setTotalPrice(100.0);
        userAppointment = appointmentRepository.save(appt);

        Receipt receipt = new Receipt();
        receipt.setAppointment(userAppointment);
        receipt.setTotalAmount(BigDecimal.valueOf(100.0));
        receipt.setPaymentMethod("Cash");
        receipt.setNotes("Test note");
        testReceipt = receiptRepository.save(receipt);
    }

    @Test
    void getAllReceipts_shouldSucceed_forAdmin() throws Exception {
        mockMvc.perform(get("/api/receipts").header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].receiptId").value(testReceipt.getReceiptId()));
    }

    @Test
    void getAllReceipts_shouldFail_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/receipts").header("Authorization", userToken))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Access denied"));
    }

    @Test
    void getReceiptById_shouldReturnReceipt_forAdmin() throws Exception {
        mockMvc.perform(get("/api/receipts/" + testReceipt.getReceiptId()).header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receiptId").value(testReceipt.getReceiptId()));
    }

    @Test
    void getReceiptById_shouldFail_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/receipts/" + testReceipt.getReceiptId()).header("Authorization", userToken))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Access denied"));
    }

    @Test
    void getReceiptById_shouldReturn404_ifNotFound() throws Exception {
        mockMvc.perform(get("/api/receipts/99999").header("Authorization", adminToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void getReceiptByAppointmentId_shouldSucceed_forOwner() throws Exception {
        mockMvc.perform(get("/api/receipts/appointment/" + userAppointment.getAppointmentId()).header("Authorization", userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receiptId").value(testReceipt.getReceiptId()));
    }

    @Test
    void getReceiptByAppointmentId_shouldSucceed_forAdmin() throws Exception {
        mockMvc.perform(get("/api/receipts/appointment/" + userAppointment.getAppointmentId()).header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receiptId").value(testReceipt.getReceiptId()));
    }

    @Test
    void getReceiptByAppointmentId_shouldFail_forOtherUser() throws Exception {
        User other = new User();
        other.setName("Other");
        other.setEmail("other@example.com");
        other.setPasswordHash("pass");
        other.setIsAdmin(false);
        userRepository.save(other);

        String otherToken = "Bearer " + jwtService.generateToken(other);

        mockMvc.perform(get("/api/receipts/appointment/" + userAppointment.getAppointmentId()).header("Authorization", otherToken))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Access denied"));
    }

    @Test
    void getReceiptByAppointmentId_shouldReturn404_ifNotFound() throws Exception {
        mockMvc.perform(get("/api/receipts/appointment/99999").header("Authorization", adminToken))
            .andExpect(status().isNotFound());
    }
}