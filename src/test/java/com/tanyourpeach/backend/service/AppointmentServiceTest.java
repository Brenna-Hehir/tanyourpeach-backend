package com.tanyourpeach.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import com.tanyourpeach.backend.model.*;
import com.tanyourpeach.backend.repository.*;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AppointmentServiceTest {

    @InjectMocks
    private AppointmentService appointmentService;

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private FinancialLogRepository financialLogRepository;
    @Mock
    private TanServiceRepository tanServiceRepository;
    @Mock
    private ServiceInventoryUsageRepository usageRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private AppointmentStatusHistoryRepository statusHistoryRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest request;

    private Appointment testAppointment;
    private Availability testSlot;
    private TanService testService;

    @BeforeEach
    void setUp() {
        testService = new TanService();
        testService.setServiceId(1L);
        testService.setBasePrice(50.0);

        testSlot = new Availability();
        testSlot.setSlotId(1L);
        testSlot.setIsBooked(false);
        testSlot.setDate(LocalDate.now());
        testSlot.setStartTime(LocalTime.of(14, 0));

        testAppointment = new Appointment();
        testAppointment.setService(testService);
        testAppointment.setClientEmail("client@example.com");
        testAppointment.setClientName("Test Client");
        testAppointment.setAvailability(testSlot);
    }

    @Test
    void createAppointment_shouldSucceed_whenSlotAvailable() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.save(any())).thenAnswer(i -> {
            Appointment a = i.getArgument(0);
            a.setAppointmentId(99L);
            return a;
        });

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);

        assertTrue(result.isPresent());
        assertEquals(Appointment.Status.PENDING, result.get().getStatus());
        assertEquals(50.0, result.get().getBasePrice());
        assertNotNull(result.get().getAppointmentDateTime());
    }

    @Test
    void createAppointment_shouldFail_whenSlotBooked() {
        testSlot.setIsBooked(true);
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(testSlot));

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateAppointment_shouldUpdateFields_andConfirmStatus() {
        testAppointment.setAppointmentId(1L);
        testAppointment.setStatus(Appointment.Status.PENDING);

        Appointment updated = new Appointment();
        updated.setService(testService);
        updated.setClientEmail("client@example.com");
        updated.setClientName("Updated Client");
        updated.setStatus(Appointment.Status.CONFIRMED);
        updated.setAppointmentDateTime(LocalDateTime.now());

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(usageRepository.findByService_ServiceId(1L)).thenReturn(List.of());
        when(receiptRepository.findByAppointment_AppointmentId(1L)).thenReturn(null);

        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);

        assertTrue(result.isPresent());
        assertEquals(Appointment.Status.CONFIRMED, result.get().getStatus());
    }

    @Test
    void updateAppointment_shouldFail_whenInventoryInsufficient() {
        // Existing appointment in DB with PENDING status
        Appointment existing = new Appointment();
        existing.setAppointmentId(1L);
        existing.setStatus(Appointment.Status.PENDING);
        existing.setBasePrice(100.0);

        TanService service = new TanService();
        service.setServiceId(10L);
        existing.setService(service);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));

        // ServiceInventoryUsage requires more than available
        Inventory item = new Inventory();
        item.setItemId(100L);
        item.setQuantity(3); // not enough

        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setItem(item);
        usage.setQuantityUsed(5); // more than available

        when(usageRepository.findByService_ServiceId(10L)).thenReturn(List.of(usage));
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(item));

        // Simulate client confirming the appointment
        Appointment updated = new Appointment();
        updated.setStatus(Appointment.Status.CONFIRMED);
        updated.setService(service);
        updated.setClientEmail("client@example.com");

        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);

        assertTrue(result.isEmpty()); // Expect failure due to insufficient inventory
        verify(financialLogRepository, never()).save(any());
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void deleteAppointment_shouldSucceed() {
        when(appointmentRepository.existsById(1L)).thenReturn(true);

        boolean result = appointmentService.deleteAppointment(1L);

        assertTrue(result);
        verify(appointmentRepository).deleteById(1L);
    }

    @Test
    void getGuestVsRegisteredStats_shouldReturnCounts() {
        when(appointmentRepository.countByUserIsNull()).thenReturn(3L);
        when(appointmentRepository.countByUserIsNotNull()).thenReturn(7L);

        Map<String, Long> stats = appointmentService.getGuestVsRegisteredStats();

        assertEquals(3L, stats.get("guestAppointments"));
        assertEquals(7L, stats.get("registeredAppointments"));
    }
}