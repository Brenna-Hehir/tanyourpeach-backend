package com.tanyourpeach.backend.service;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.AppointmentStatusHistory;
import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.model.ServiceInventoryUsage;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.AppointmentStatusHistoryRepository;
import com.tanyourpeach.backend.repository.AvailabilityRepository;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.ReceiptRepository;
import com.tanyourpeach.backend.repository.ServiceInventoryUsageRepository;
import com.tanyourpeach.backend.repository.TanServiceRepository;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private FinancialLogRepository financialLogRepository;

    @Autowired
    private TanServiceRepository tanServiceRepository;

    @Autowired
    private ServiceInventoryUsageRepository usageRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private AppointmentStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Enum for appointment status
    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    // GET all appointments
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // GET appointment by ID
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    // POST create new appointment
    public Optional<Appointment> createAppointment(Appointment appointment, HttpServletRequest request) {
        // Attempt to extract user from JWT if logged in
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);
            userRepository.findByEmail(email).ifPresent(appointment::setUser);
        }

        // Validate availability slot
        Long slotId = appointment.getAvailability() != null ? appointment.getAvailability().getSlotId() : null;
        if (slotId == null) return Optional.empty();

        Optional<Availability> slotOpt = availabilityRepository.findById(slotId);
        if (slotOpt.isEmpty() || slotOpt.get().getIsBooked()) return Optional.empty();

        Availability slot = slotOpt.get();
        slot.setIsBooked(true);

        // Set appointment time and default status
        appointment.setAppointmentDateTime(LocalDateTime.of(slot.getDate(), slot.getStartTime()));
        appointment.setStatus(Appointment.Status.PENDING);

        // Set base price from selected service
        if (appointment.getService() != null && appointment.getService().getServiceId() != null) {
            tanServiceRepository.findById(appointment.getService().getServiceId()).ifPresent(service -> {
                appointment.setService(service);
                appointment.setBasePrice(service.getBasePrice());
            });
        }

        // Calculate total price
        double base = appointment.getBasePrice() != null ? appointment.getBasePrice() : 0;
        double travel = appointment.getTravelFee() != null ? appointment.getTravelFee() : 0;
        appointment.setTotalPrice(base + travel);

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Link the slot to the appointment
        slot.setAppointment(savedAppointment);
        availabilityRepository.save(slot);

        // Save initial status change (PENDING)
        AppointmentStatusHistory history = new AppointmentStatusHistory();
        history.setAppointment(savedAppointment);
        history.setStatus(Appointment.Status.PENDING.name());

        // Use user or email as the "changed by"
        if (appointment.getUser() != null) {
            history.setChangedByUser(appointment.getUser());
        } else {
            history.setChangedByClientEmail(appointment.getClientEmail());
        }

        statusHistoryRepository.save(history);

        return Optional.of(savedAppointment);
    }

    // PUT update appointment
    public Optional<Appointment> updateAppointment(Long id, Appointment updated, HttpServletRequest request) {
        // Fetch existing appointment
        Optional<Appointment> existingOpt = appointmentRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        Appointment existing = existingOpt.get();

        Appointment.Status oldStatus = existing.getStatus();

        // Basic field updates
        existing.setClientName(updated.getClientName());
        existing.setClientEmail(updated.getClientEmail());
        existing.setClientAddress(updated.getClientAddress());
        existing.setAppointmentDateTime(updated.getAppointmentDateTime());
        existing.setDistanceMiles(updated.getDistanceMiles());
        existing.setTravelFee(updated.getTravelFee());
        existing.setNotes(updated.getNotes());
        existing.setStatus(updated.getStatus());

        // Update service and base price
        if (updated.getService() != null && updated.getService().getServiceId() != null) {
            tanServiceRepository.findById(updated.getService().getServiceId()).ifPresent(service -> {
                existing.setService(service);
                existing.setBasePrice(service.getBasePrice());
            });
        }

        // Recalculate total price
        double total = existing.getBasePrice() + (existing.getTravelFee() != null ? existing.getTravelFee() : 0);
        existing.setTotalPrice(total);

        // Update availability slot if provided
        if (!oldStatus.equals(updated.getStatus())) {
            // Save status change to history if status changed
            AppointmentStatusHistory history = new AppointmentStatusHistory();
            history.setAppointment(existing);
            history.setStatus(updated.getStatus().name());

            // Set who made the change
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtService.extractUsername(token);
                userRepository.findByEmail(email).ifPresent(history::setChangedByUser);
            } else {
                history.setChangedByClientEmail(updated.getClientEmail());
            }

            statusHistoryRepository.save(history);
        }

        // Deduct inventory if status changed to CONFIRMED
        if (oldStatus != Appointment.Status.CONFIRMED && updated.getStatus() == Appointment.Status.CONFIRMED) {
            Long serviceId = existing.getService().getServiceId();
            List<ServiceInventoryUsage> usages = usageRepository.findByService_ServiceId(serviceId);

            // Check for sufficient inventory
            boolean hasInsufficientInventory = usages.stream().anyMatch(usage -> {
                Long itemId = usage.getItem().getItemId();
                int quantityToDeduct = usage.getQuantityUsed();
                return inventoryRepository.findById(itemId)
                    .map(item -> item.getQuantity() < quantityToDeduct)
                    .orElse(true); // Fail if item missing
            });

            if (hasInsufficientInventory) return Optional.empty();

            // Deduct inventory items
            for (ServiceInventoryUsage usage : usages) {
                Long itemId = usage.getItem().getItemId();
                int quantityToDeduct = usage.getQuantityUsed();
                inventoryRepository.findById(itemId).ifPresent(item -> {
                    item.setQuantity(item.getQuantity() - quantityToDeduct);
                    inventoryRepository.save(item);
                });
            }

            // Log revenue
            FinancialLog log = new FinancialLog();
            log.setType(FinancialLog.Type.revenue);
            log.setSource("appointment");
            log.setReferenceId(existing.getAppointmentId());
            log.setAmount(BigDecimal.valueOf(existing.getTotalPrice()));
            log.setDescription("Confirmed appointment for " + existing.getClientName());
            financialLogRepository.save(log);
        }

        // Auto-generate receipt if confirming for the first time
        if (updated.getStatus() == Appointment.Status.CONFIRMED) {
            Receipt existingReceipt = receiptRepository.findByAppointment_AppointmentId(existing.getAppointmentId());
            if (existingReceipt == null) {
                Receipt receipt = new Receipt();
                receipt.setAppointment(existing);
                receipt.setTotalAmount(BigDecimal.valueOf(existing.getTotalPrice()));
                receipt.setPaymentMethod("Unpaid");
                receiptRepository.save(receipt);
            }
        }

        // Save updated appointment
        Appointment saved = appointmentRepository.save(existing);
        return Optional.of(saved);
    }

    // GET appointment stats
    public Map<String, Long> getGuestVsRegisteredStats() {
        long guestCount = appointmentRepository.countByUserIsNull();
        long registeredCount = appointmentRepository.countByUserIsNotNull();

        Map<String, Long> stats = new HashMap<>();
        stats.put("guestAppointments", guestCount);
        stats.put("registeredAppointments", registeredCount);
        return stats;
    }

    // DELETE appointment
    public boolean deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) return false;
        appointmentRepository.deleteById(id);
        return true;
    }
}