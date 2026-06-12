package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.*;
import com.tanyourpeach.backend.repository.*;
import com.tanyourpeach.backend.util.TestDataCleaner;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AppointmentServiceTransactionIntegrationTest {

    @Autowired private AppointmentService appointmentService;
    @Autowired private TestDataCleaner testDataCleaner;
    @MockitoSpyBean private AppointmentRepository appointmentRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private TanServiceRepository tanServiceRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ServiceInventoryUsageRepository usageRepository;
    @Autowired private FinancialLogRepository financialLogRepository;

    @MockitoSpyBean
    private AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;

    @MockitoSpyBean
    private ReceiptRepository receiptRepository;

    @MockitoSpyBean
    private AppointmentRepository appointmentRepositorySpy;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        testDataCleaner.cleanAll();

        request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
    }

    @Test
    void createAppointment_shouldRollbackAppointmentAndAvailability_whenStatusHistorySaveFails() {
        TanService service = saveService();
        Availability availability = saveAvailability(false);

        Appointment appointment = new Appointment();
        appointment.setService(serviceRef(service));
        appointment.setAvailability(availabilityRef(availability));
        appointment.setClientName("Rollback Client");
        appointment.setClientEmail("rollback-create@example.com");
        appointment.setClientAddress("123 Peach St");
        appointment.setTravelFee(10.0);

        doThrow(new RuntimeException("forced status history failure"))
                .when(appointmentStatusHistoryRepository)
                .save(any(AppointmentStatusHistory.class));

        assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(appointment, request));

        assertTrue(appointmentRepository.findAll().isEmpty());
        assertTrue(appointmentStatusHistoryRepository.findAll().isEmpty());

        Availability reloadedAvailability =
                availabilityRepository.findById(availability.getSlotId()).orElseThrow();

        assertFalse(Boolean.TRUE.equals(reloadedAvailability.getIsBooked()));
    }

    @Test
    void updateAppointment_shouldRollbackConfirmationSideEffects_whenReceiptSaveFails() {
        TanService service = saveService();
        Availability availability = saveAvailability(true);
        Appointment appointment = savePendingAppointment(service, availability);
        Inventory inventory = saveInventory(10);
        saveUsage(service, inventory, 2);

        doThrow(new RuntimeException("forced receipt failure"))
                .when(receiptRepository)
                .save(any(Receipt.class));

        Appointment updated = new Appointment();
        updated.setService(serviceRef(service));
        updated.setAvailability(availabilityRef(availability));
        updated.setClientName("Confirmed Client");
        updated.setClientEmail("rollback-confirm@example.com");
        updated.setClientAddress("456 Peach Ave");
        updated.setAppointmentDateTime(LocalDateTime.of(availability.getDate(), availability.getStartTime()));
        updated.setTravelFee(5.0);
        updated.setStatus(Appointment.Status.CONFIRMED);

        assertThrows(RuntimeException.class,
                () -> appointmentService.updateAppointment(appointment.getAppointmentId(), updated, request));

        Appointment reloadedAppointment =
                appointmentRepository.findById(appointment.getAppointmentId()).orElseThrow();

        assertEquals(Appointment.Status.PENDING, reloadedAppointment.getStatus());

        Inventory reloadedInventory =
                inventoryRepository.findById(inventory.getItemId()).orElseThrow();

        assertEquals(10, reloadedInventory.getQuantity());

        assertTrue(financialLogRepository.findAll().isEmpty());
        assertTrue(receiptRepository.findAll().isEmpty());

        assertFalse(appointmentStatusHistoryRepository.findAll().stream()
                .anyMatch(history -> "CONFIRMED".equals(history.getStatus())));
    }

    @Test
    void deleteAppointment_shouldRollbackAvailabilityAndHistoryDelete_whenAppointmentDeleteFails() {
        TanService service = saveService();
        Availability availability = saveAvailability(true);
        Appointment appointment = savePendingAppointment(service, availability);

        AppointmentStatusHistory history = new AppointmentStatusHistory();
        history.setAppointment(appointment);
        history.setStatus("PENDING");
        history.setChangedAt(LocalDateTime.now());
        appointmentStatusHistoryRepository.save(history);

        doThrow(new RuntimeException("forced appointment delete failure"))
                .when(appointmentRepository)
                .deleteById(appointment.getAppointmentId());

        assertThrows(RuntimeException.class,
                () -> appointmentService.deleteAppointment(appointment.getAppointmentId()));

        Appointment reloadedAppointment =
                appointmentRepository.findById(appointment.getAppointmentId()).orElseThrow();

        Availability reloadedAvailability =
                availabilityRepository.findById(availability.getSlotId()).orElseThrow();

        assertNotNull(reloadedAppointment);
        assertTrue(Boolean.TRUE.equals(reloadedAvailability.getIsBooked()));

        assertFalse(appointmentStatusHistoryRepository.findAll().isEmpty());
    }

    private TanService saveService() {
        TanService service = new TanService();
        service.setName("Rollback Glow");
        service.setBasePrice(50.0);
        service.setDurationMinutes(30);
        service.setIsActive(true);
        return tanServiceRepository.save(service);
    }

    private Availability saveAvailability(boolean booked) {
        Availability availability = new Availability();
        availability.setDate(LocalDate.now().plusDays(1));
        availability.setStartTime(LocalTime.of(14, 0));
        availability.setEndTime(LocalTime.of(14, 30));
        availability.setIsBooked(booked);
        return availabilityRepository.save(availability);
    }

    private Appointment savePendingAppointment(TanService service, Availability availability) {
        Appointment appointment = new Appointment();
        appointment.setService(service);
        appointment.setAvailability(availability);
        appointment.setClientName("Pending Client");
        appointment.setClientEmail("pending@example.com");
        appointment.setClientAddress("123 Peach St");
        appointment.setAppointmentDateTime(LocalDateTime.of(availability.getDate(), availability.getStartTime()));
        appointment.setBasePrice(service.getBasePrice());
        appointment.setTravelFee(5.0);
        appointment.setTotalPrice(55.0);
        appointment.setStatus(Appointment.Status.PENDING);
        return appointmentRepository.save(appointment);
    }

    private Inventory saveInventory(int quantity) {
        Inventory inventory = new Inventory();
        inventory.setItemName("Tanning Solution");
        inventory.setQuantity(quantity);
        inventory.setUnitCost(BigDecimal.valueOf(12.50));
        inventory.setTotalSpent(BigDecimal.valueOf(125.00));
        inventory.setLowStockThreshold(1);
        return inventoryRepository.save(inventory);
    }

    private void saveUsage(TanService service, Inventory inventory, int quantityUsed) {
        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setService(service);
        usage.setItem(inventory);
        usage.setId(new ServiceInventoryUsageKey(service.getServiceId(), inventory.getItemId()));
        usage.setQuantityUsed(quantityUsed);
        usageRepository.save(usage);
    }

    private TanService serviceRef(TanService service) {
        TanService ref = new TanService();
        ref.setServiceId(service.getServiceId());
        return ref;
    }

    private Availability availabilityRef(Availability availability) {
        Availability ref = new Availability();
        ref.setSlotId(availability.getSlotId());
        return ref;
    }
}