package com.tanyourpeach.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    void getAllAppointments_shouldReturnAllAppointments() {
        List<Appointment> mockList = List.of(new Appointment(), new Appointment());

        when(appointmentRepository.findAll()).thenReturn(mockList);

        List<Appointment> result = appointmentService.getAllAppointments();

        assertEquals(2, result.size());
    }

    @Test
    void getAppointmentById_shouldReturnAppointmentIfExists() {
        Appointment mockAppointment = new Appointment();
        mockAppointment.setAppointmentId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));

        Optional<Appointment> result = appointmentService.getAppointmentById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getAppointmentId());
    }

    @Test
    void getGuestVsRegisteredStats_shouldReturnCounts() {
        when(appointmentRepository.countByUserIsNull()).thenReturn(3L);
        when(appointmentRepository.countByUserIsNotNull()).thenReturn(7L);

        Map<String, Long> stats = appointmentService.getGuestVsRegisteredStats();

        assertEquals(3L, stats.get("guestAppointments"));
        assertEquals(7L, stats.get("registeredAppointments"));
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
    void createAppointment_shouldSaveStatusHistory_forGuest() {
        testAppointment.setAvailability(testSlot);
        testAppointment.setClientEmail("guest@example.com");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.save(any())).thenAnswer(i -> {
            Appointment a = i.getArgument(0);
            a.setAppointmentId(123L);
            return a;
        });

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);

        assertTrue(result.isPresent());
        verify(statusHistoryRepository).save(argThat(history ->
            history.getStatus().equals("PENDING") &&
            history.getChangedByClientEmail().equals("guest@example.com")
        ));
    }

    @Test
    void createAppointment_shouldLinkSlotToAppointment() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.save(any())).thenAnswer(i -> {
            Appointment a = i.getArgument(0);
            a.setAppointmentId(123L);
            return a;
        });

        when(availabilityRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);

        assertTrue(result.isPresent());
        verify(availabilityRepository).save(argThat(slot ->
            slot.getIsBooked() &&
            slot.getAppointment() != null &&
            slot.getAppointment().getAppointmentId().equals(123L)
        ));
    }

    @Test
    void createAppointment_shouldLinkUserIfTokenPresent() {
        String token = "fake-jwt-token";
        String email = "testuser@example.com";

        User mockUser = new User();
        mockUser.setUserId(42L);
        mockUser.setEmail(email);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.save(any())).thenAnswer(i -> {
            Appointment a = i.getArgument(0);
            a.setAppointmentId(555L);
            return a;
        });

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);

        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get().getUser());
    }

    @Test
    void createAppointment_shouldHandleMissingServiceGracefully() {
        testAppointment.setService(null); // simulate missing service
        testAppointment.setAvailability(testSlot); // ensure slot is present
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(availabilityRepository.save(any())).thenReturn(testSlot);

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);

        assertTrue(result.isPresent());
        assertEquals(Appointment.Status.PENDING, result.get().getStatus());
    }

    @Test
    void createAppointment_shouldSetBasePriceToZero_whenServiceNotFound() {
        // Arrange
        Appointment appointment = new Appointment();
        appointment.setClientName("Jane Doe");
        appointment.setClientEmail("jane@example.com");
        appointment.setTravelFee(20.0);

        // No service set (or you could set a service with null ID)
        appointment.setService(null);

        // Create a dummy availability slot
        Availability slot = new Availability();
        slot.setSlotId(1L);
        slot.setIsBooked(false);
        slot.setDate(LocalDate.now());
        slot.setStartTime(LocalTime.of(10, 0));
        appointment.setAvailability(slot);

        // Mock repository behavior
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(statusHistoryRepository.save(any())).thenReturn(null); // ignore result

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Authorization")).thenReturn(null); // No auth token

        // Act
        Optional<Appointment> result = appointmentService.createAppointment(appointment, mockRequest);

        // Assert
        assertTrue(result.isPresent());
        Appointment saved = result.get();
        assertEquals(0.0, saved.getBasePrice());
        assertEquals(20.0, saved.getTravelFee());
        assertEquals(20.0, saved.getTotalPrice());
    }

    @Test
    void createAppointment_shouldFail_whenSlotBooked() {
        testSlot.setIsBooked(true);
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(testSlot));

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void createAppointment_shouldFail_whenSlotMissing() {
        testAppointment.setAvailability(null); // no slot
        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);
        assertTrue(result.isEmpty());
    }

    @Test
    void createAppointment_shouldFail_whenSlotNotFound() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);
        assertTrue(result.isEmpty());
    }

    @Test
    void createAppointment_shouldFail_whenClientFieldsInvalid() {
        testAppointment.setClientName("  "); // blank
        testAppointment.setClientEmail(null); // null

        Optional<Appointment> result = appointmentService.createAppointment(testAppointment, request);
        assertTrue(result.isEmpty());
    }

    @Test
    void createAppointment_shouldFail_whenTravelFeeNegative() {
        testAppointment.setTravelFee(-5.0);

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
    void updateAppointment_shouldConfirmWithInventoryAndGenerateLogAndReceipt() {
        Appointment existing = new Appointment();
        existing.setAppointmentId(1L);
        existing.setStatus(Appointment.Status.PENDING);
        existing.setClientName("Peachy");
        existing.setBasePrice(80.0);
        existing.setTravelFee(10.0);

        TanService service = new TanService();
        service.setServiceId(20L);
        service.setBasePrice(80.0);
        existing.setService(service);

        Appointment updated = new Appointment();
        updated.setStatus(Appointment.Status.CONFIRMED);
        updated.setService(service);
        updated.setClientEmail("client@example.com");
        updated.setClientName("Peachy");
        updated.setTravelFee(10.0);

        Inventory item = new Inventory();
        item.setItemId(200L);
        item.setQuantity(10); // enough inventory

        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setItem(item);
        usage.setQuantityUsed(5);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tanServiceRepository.findById(20L)).thenReturn(Optional.of(service));
        when(usageRepository.findByService_ServiceId(20L)).thenReturn(List.of(usage));
        when(inventoryRepository.findById(200L)).thenReturn(Optional.of(item));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(receiptRepository.findByAppointment_AppointmentId(1L)).thenReturn(null);

        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);

        assertTrue(result.isPresent());
        verify(inventoryRepository).save(any()); // inventory deducted
        verify(financialLogRepository).save(any()); // log generated
        verify(receiptRepository).save(any()); // receipt generated

        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(receiptCaptor.capture());
        Receipt savedReceipt = receiptCaptor.getValue();

        assertEquals("Unpaid", savedReceipt.getPaymentMethod());
        assertEquals(BigDecimal.valueOf(90.0), savedReceipt.getTotalAmount());
    }

    @Test
    void updateAppointment_shouldSkipStatusLogic_whenStatusUnchanged() {
        testAppointment.setAppointmentId(1L);
        testAppointment.setStatus(Appointment.Status.PENDING);

        Appointment updated = new Appointment();
        updated.setStatus(Appointment.Status.PENDING); // no change
        updated.setService(testService);
        updated.setClientEmail("client@example.com");
        updated.setClientName("Test Client");
        updated.setTravelFee(0.0);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);

        assertTrue(result.isPresent());
        verify(statusHistoryRepository, never()).save(any());
        verify(financialLogRepository, never()).save(any());
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void updateAppointment_shouldNotUpdate_ifServiceMissing() {
        Long appointmentId = 1L;

        // Existing appointment with valid service
        Appointment existing = new Appointment();
        existing.setAppointmentId(appointmentId);
        existing.setClientName("Bob");
        existing.setClientEmail("bob@example.com");
        existing.setStatus(Appointment.Status.PENDING);
        existing.setTravelFee(10.0);
        existing.setBasePrice(40.0);

        TanService existingService = new TanService();
        existingService.setServiceId(5L); // must be non-null
        existingService.setBasePrice(40.0);
        existing.setService(existingService); // must be set

        // Updated appointment with null service
        Appointment updated = new Appointment();
        updated.setClientName("Bob");
        updated.setClientEmail("bob@example.com");
        updated.setStatus(Appointment.Status.CONFIRMED); // triggers inventory logic
        updated.setTravelFee(10.0);
        updated.setService(null); // Missing service triggers validation

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        Optional<Appointment> result = appointmentService.updateAppointment(appointmentId, updated, request);

        // Assert
        assertTrue(result.isEmpty());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void updateAppointment_shouldNotCreateReceipt_ifAlreadyExists() {
        Appointment existing = new Appointment();
        existing.setAppointmentId(1L);
        existing.setStatus(Appointment.Status.PENDING);
        existing.setBasePrice(100.0);
        existing.setTravelFee(20.0);

        TanService service = new TanService();
        service.setServiceId(42L);
        existing.setService(service);

        Appointment updated = new Appointment();
        updated.setStatus(Appointment.Status.CONFIRMED);
        updated.setService(service);
        updated.setClientEmail("client@example.com");
        updated.setClientName("Existing Client");
        updated.setTravelFee(20.0);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tanServiceRepository.findById(42L)).thenReturn(Optional.of(service));
        when(usageRepository.findByService_ServiceId(42L)).thenReturn(List.of());
        when(receiptRepository.findByAppointment_AppointmentId(1L)).thenReturn(new Receipt()); // already exists
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);

        assertTrue(result.isPresent());
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void updateAppointment_shouldNotDeductInventory_ifNotConfirmed() {
        // Arrange
        Long appointmentId = 1L;

        // Existing appointment (not confirmed)
        Appointment existing = new Appointment();
        existing.setAppointmentId(appointmentId);
        existing.setClientName("Alice");
        existing.setClientEmail("alice@example.com");
        existing.setStatus(Appointment.Status.PENDING);
        existing.setTravelFee(15.0);
        existing.setBasePrice(50.0);

        TanService service = new TanService();
        service.setServiceId(10L);
        service.setBasePrice(50.0);
        existing.setService(service);

        // Updated appointment — still not confirmed
        Appointment updated = new Appointment();
        updated.setClientName("Alice");
        updated.setClientEmail("alice@example.com");
        updated.setStatus(Appointment.Status.PENDING); // no status change
        updated.setTravelFee(15.0);
        updated.setService(service);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(tanServiceRepository.findById(10L)).thenReturn(Optional.of(service));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null); // No auth needed

        // Act
        Optional<Appointment> result = appointmentService.updateAppointment(appointmentId, updated, request);

        // Assert
        assertTrue(result.isPresent());
        Appointment saved = result.get();

        // Make sure total price is still correct
        assertEquals(65.0, saved.getTotalPrice());

        // Verify inventoryRepository was NOT called (no deduction)
        verify(inventoryRepository, never()).save(any());
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
    void updateAppointment_shouldFail_whenClientFieldsInvalid() {
        testAppointment.setAppointmentId(1L);
        testAppointment.setStatus(Appointment.Status.PENDING);

        Appointment updated = new Appointment();
        updated.setClientName(" ");
        updated.setClientEmail("");
        updated.setStatus(Appointment.Status.CONFIRMED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateAppointment_shouldFail_whenTravelFeeNegative() {
        testAppointment.setAppointmentId(1L);
        testAppointment.setStatus(Appointment.Status.PENDING);

        Appointment updated = new Appointment();
        updated.setClientName("Client");
        updated.setClientEmail("client@example.com");
        updated.setStatus(Appointment.Status.CONFIRMED);
        updated.setTravelFee(-2.0);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateAppointment_shouldFail_whenAppointmentNotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
        Appointment updated = new Appointment();
        Optional<Appointment> result = appointmentService.updateAppointment(1L, updated, request);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteAppointment_shouldSucceed() {
        when(appointmentRepository.existsById(1L)).thenReturn(true);

        boolean result = appointmentService.deleteAppointment(1L);

        assertTrue(result);
        verify(appointmentRepository).deleteById(1L);
    }

    @Test
    void deleteAppointment_shouldReturnFalse_whenAppointmentNotFound() {
        when(appointmentRepository.existsById(1L)).thenReturn(false);
        assertFalse(appointmentService.deleteAppointment(1L));
    }
}