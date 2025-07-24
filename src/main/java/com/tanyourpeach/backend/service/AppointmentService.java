package com.tanyourpeach.backend.service;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.AppointmentStatusHistory;
import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.model.ServiceInventoryUsage;
import com.tanyourpeach.backend.model.TanService;
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

    // GET appointment stats
    public Map<String, Long> getGuestVsRegisteredStats() {
        long guestCount = appointmentRepository.countByUserIsNull();
        long registeredCount = appointmentRepository.countByUserIsNotNull();

        Map<String, Long> stats = new HashMap<>();
        stats.put("guestAppointments", guestCount);
        stats.put("registeredAppointments", registeredCount);
        return stats;
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

        // Validate client info
        if (appointment.getClientName() == null || appointment.getClientName().trim().isEmpty()) return Optional.empty();
        if (appointment.getClientEmail() == null || appointment.getClientEmail().trim().isEmpty()) return Optional.empty();
        if (appointment.getClientAddress() == null || appointment.getClientAddress().trim().isEmpty()) return Optional.empty();
        if (appointment.getTravelFee() != null && appointment.getTravelFee() < 0) return Optional.empty();

        // Validate availability slot
        if (appointment.getAvailability() == null || appointment.getAvailability().getSlotId() == null) {
            return Optional.empty();
        }

        Long slotId = appointment.getAvailability().getSlotId();
        Optional<Availability> slotOpt = availabilityRepository.findById(slotId);
        if (slotOpt.isEmpty()) return Optional.empty();

        Availability slot = slotOpt.get();
        if (slot.getIsBooked()) return Optional.empty();

        appointment.setAvailability(slot); // set the fully loaded Availability object
        slot.setIsBooked(true); // mark as booked

        // Set appointment time and default status
        appointment.setAppointmentDateTime(LocalDateTime.of(slot.getDate(), slot.getStartTime()));
        appointment.setStatus(Appointment.Status.PENDING);

        // Set base price from selected service
        if (appointment.getService() != null && appointment.getService().getServiceId() != null) {
            tanServiceRepository.findById(appointment.getService().getServiceId()).ifPresent(service -> {
                appointment.setService(service);
                appointment.setBasePrice(service.getBasePrice());
            });
        } else {
            appointment.setBasePrice(0.0);
        }

        // Calculate total price
        double base = appointment.getBasePrice() != null ? appointment.getBasePrice() : 0;
        double travel = appointment.getTravelFee() != null ? appointment.getTravelFee() : 0;
        appointment.setTotalPrice(base + travel);

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        availabilityRepository.save(slot);

        // Save initial status change (PENDING)
        AppointmentStatusHistory history = new AppointmentStatusHistory();
        history.setAppointment(savedAppointment);
        history.setStatus(Appointment.Status.PENDING.name());

        // Use user or email as the "changed by"
        if (appointment.getUser() != null) {
            history.setChangedByUser(appointment.getUser());
        } else {
            history.setchangedByEmail(appointment.getClientEmail());
        }

        statusHistoryRepository.save(history);

        return Optional.of(savedAppointment);
    }

    // PUT update appointment
    public Optional<Appointment> updateAppointment(Long id, Appointment updated, HttpServletRequest request) {
        Optional<Appointment> existingOpt = appointmentRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();
        Appointment existing = existingOpt.get();

        Appointment.Status oldStatus = existing.getStatus();

        // Validate input fields
        if (updated.getClientName() == null || updated.getClientName().trim().isEmpty()) return Optional.empty();
        if (updated.getClientEmail() == null || updated.getClientEmail().trim().isEmpty()) return Optional.empty();
        if (updated.getClientAddress() == null || updated.getClientAddress().trim().isEmpty()) return Optional.empty();
        if (updated.getTravelFee() != null && updated.getTravelFee() < 0) return Optional.empty();
        if (updated.getService() == null || updated.getService().getServiceId() == null) return Optional.empty();

        // Load new service
        TanService newService = null;
        if (updated.getService() != null && updated.getService().getServiceId() != null) {
            Optional<TanService> serviceOpt = tanServiceRepository.findById(updated.getService().getServiceId());
            if (serviceOpt.isEmpty()) return Optional.empty();
            newService = serviceOpt.get();
        }

        // Check inventory early if status is changing to CONFIRMED
        if (oldStatus != Appointment.Status.CONFIRMED && updated.getStatus() == Appointment.Status.CONFIRMED) {
            Long serviceId = newService != null
                ? newService.getServiceId()
                : (existing.getService() != null ? existing.getService().getServiceId() : null);

            List<ServiceInventoryUsage> usages = usageRepository.findByService_ServiceId(serviceId);

            boolean hasInsufficientInventory = usages.stream().anyMatch(usage -> {
                Long itemId = usage.getItem().getItemId();
                int quantityToDeduct = usage.getQuantityUsed();
                return inventoryRepository.findById(itemId)
                    .map(item -> item.getQuantity() < quantityToDeduct)
                    .orElse(true);
            });

            if (hasInsufficientInventory) return Optional.empty();
        }

        // Handle availability change
        if (updated.getAvailability() != null && updated.getAvailability().getSlotId() != null) {
            Long newSlotId = updated.getAvailability().getSlotId();
            Availability currentSlot = existing.getAvailability();
            boolean isChangingSlot = currentSlot == null || !newSlotId.equals(currentSlot.getSlotId());

            if (isChangingSlot) {
                Optional<Availability> newSlotOpt = availabilityRepository.findById(newSlotId);
                if (newSlotOpt.isEmpty() || Boolean.TRUE.equals(newSlotOpt.get().getIsBooked())) {
                    return Optional.empty();
                }

                Availability newSlot = newSlotOpt.get();

                if (currentSlot != null) {
                    currentSlot.setIsBooked(false);
                    availabilityRepository.save(currentSlot);
                }

                newSlot.setIsBooked(true);
                availabilityRepository.save(newSlot);

                existing.setAvailability(newSlot);
                existing.setAppointmentDateTime(LocalDateTime.of(newSlot.getDate(), newSlot.getStartTime()));
            }
        } else {
            return Optional.empty(); // invalid slot
        }

        // Apply field updates
        existing.setClientName(updated.getClientName());
        existing.setClientEmail(updated.getClientEmail());
        existing.setClientAddress(updated.getClientAddress());
        existing.setAppointmentDateTime(updated.getAppointmentDateTime());
        existing.setDistanceMiles(updated.getDistanceMiles());
        existing.setTravelFee(updated.getTravelFee());
        existing.setNotes(updated.getNotes());

        if (newService != null) {
            existing.setService(newService);
            existing.setBasePrice(newService.getBasePrice());

            // Recalculate total price
            double base = existing.getBasePrice() != null ? existing.getBasePrice() : 0.0;
            double travel = existing.getTravelFee() != null ? existing.getTravelFee() : 0.0;
            existing.setTotalPrice(base + travel);
        }

        // Status change history
        if (!oldStatus.equals(updated.getStatus())) {
            AppointmentStatusHistory history = new AppointmentStatusHistory();
            history.setAppointment(existing);
            history.setStatus(updated.getStatus().name());

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtService.extractUsername(token);
                userRepository.findByEmail(email).ifPresent(history::setChangedByUser);
            } else {
                history.setchangedByEmail(updated.getClientEmail());
            }

            statusHistoryRepository.save(history);
        }

        // Deduct inventory after confirming sufficient inventory
        if (oldStatus != Appointment.Status.CONFIRMED && updated.getStatus() == Appointment.Status.CONFIRMED) {
            List<ServiceInventoryUsage> usages = usageRepository.findByService_ServiceId(existing.getService().getServiceId());
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

        // Generate receipt if confirming for first time
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

        existing.setStatus(updated.getStatus());

        Appointment saved = appointmentRepository.save(existing);
        return Optional.of(saved);
    }

    // DELETE appointment
    @Transactional
    public boolean deleteAppointment(Long id) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isEmpty()) return false;

        Appointment appointment = appointmentOpt.get();
        Availability slot = appointment.getAvailability();
        if (slot != null) {
            slot.setIsBooked(false);
            availabilityRepository.save(slot);
        }

        statusHistoryRepository.deleteAllByAppointment_AppointmentId(id);

        appointmentRepository.deleteById(id);
        return true;
    }
}