package com.tanurpeach.backend.service;

import com.tanurpeach.backend.model.Appointment;
import com.tanurpeach.backend.model.Availability;
import com.tanurpeach.backend.repository.AppointmentRepository;
import com.tanurpeach.backend.repository.AvailabilityRepository;
import com.tanurpeach.backend.repository.TanServiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private TanServiceRepository tanServiceRepository;

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
