package com.tanyourpeach.backend.service;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Availability;
import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.model.ServiceInventoryUsage;
import com.tanyourpeach.backend.model.ServiceInventoryUsageKey;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.AvailabilityRepository;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.ReceiptRepository;
import com.tanyourpeach.backend.repository.ServiceInventoryUsageRepository;
import com.tanyourpeach.backend.repository.TanServiceRepository;
import com.tanyourpeach.backend.repository.FinancialLogRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Enumerated(EnumType.STRING)
    private Status status;

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
    public Optional<Appointment> createAppointment(Appointment appointment) {
        Long slotId = appointment.getAvailability() != null ? appointment.getAvailability().getSlotId() : null;
        if (slotId == null) return Optional.empty();

        Optional<Availability> slotOpt = availabilityRepository.findById(slotId);
        if (slotOpt.isEmpty() || slotOpt.get().getIsBooked()) return Optional.empty();

        Availability slot = slotOpt.get();
        slot.setIsBooked(true);

        appointment.setAppointmentDateTime(LocalDateTime.of(slot.getDate(), slot.getStartTime()));
        Appointment savedAppointment = appointmentRepository.save(appointment);

        slot.setAppointment(savedAppointment);
        availabilityRepository.save(slot);

        return Optional.of(savedAppointment);
    }

    // PUT update appointment
    public Optional<Appointment> updateAppointment(Long id, Appointment updated) {
        Optional<Appointment> existingOpt = appointmentRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        Appointment existing = existingOpt.get();
        Appointment.Status oldStatus = existing.getStatus();

        existing.setClientName(updated.getClientName());
        existing.setClientEmail(updated.getClientEmail());
        existing.setClientAddress(updated.getClientAddress());
        existing.setAppointmentDateTime(updated.getAppointmentDateTime());
        existing.setDistanceMiles(updated.getDistanceMiles());
        existing.setTravelFee(updated.getTravelFee());
        existing.setNotes(updated.getNotes());
        existing.setStatus(updated.getStatus());

        if (updated.getService() != null && updated.getService().getServiceId() != null) {
            tanServiceRepository.findById(updated.getService().getServiceId()).ifPresent(service -> {
                existing.setService(service);
                existing.setBasePrice(service.getBasePrice());
            });
        }

        double total = existing.getBasePrice() + (existing.getTravelFee() != null ? existing.getTravelFee() : 0);
        existing.setTotalPrice(total);

        // Only trigger inventory deduction if confirming appointment
        if (oldStatus != Appointment.Status.CONFIRMED && updated.getStatus() == Appointment.Status.CONFIRMED) { 
            Long serviceId = existing.getService().getServiceId();
            List<ServiceInventoryUsage> usages = usageRepository.findByService_ServiceId(serviceId);

            boolean hasInsufficientInventory = usages.stream().anyMatch(usage -> {
                Long itemId = usage.getItem().getItemId();
                int quantityToDeduct = usage.getQuantityUsed();
                return inventoryRepository.findById(itemId)
                    .map(item -> item.getQuantity() < quantityToDeduct)
                    .orElse(true); // Treat missing item as failure
            });

            if (hasInsufficientInventory) {
                return Optional.empty(); // Cancel update due to insufficient inventory
            }

            // Deduct each item
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

        // Auto-generate receipt if one doesn't already exist
        if (updated.getStatus() == Appointment.Status.CONFIRMED) {
            Receipt existingReceipt = receiptRepository.findByAppointment_AppointmentId(existing.getAppointmentId());

            if (existingReceipt == null) {
                Receipt receipt = new Receipt();
                receipt.setAppointment(existing);
                receipt.setTotalAmount(BigDecimal.valueOf(existing.getTotalPrice()));
                receipt.setPaymentMethod("Unpaid"); // You can change this later via update
                receiptRepository.save(receipt);
            }   
        }

        Appointment saved = appointmentRepository.save(existing);
        return Optional.of(saved);
    }

    // DELETE appointment
    public boolean deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) return false;
        appointmentRepository.deleteById(id);
        return true;
    }
}