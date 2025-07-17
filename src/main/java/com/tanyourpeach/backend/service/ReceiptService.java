package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.Appointment;
import com.tanyourpeach.backend.model.Receipt;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.ReceiptRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Get all receipts
    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    // Get receipt by its ID
    public Optional<Receipt> getReceiptById(Long id) {
        return receiptRepository.findById(id);
    }

    // Get receipt by appointment ID
    public Optional<Receipt> getReceiptByAppointmentId(Long appointmentId) {
        return Optional.ofNullable(receiptRepository.findByAppointment_AppointmentId(appointmentId));
    }

    // Create a new receipt for a specific appointment
    public Optional<Receipt> createReceipt(Long appointmentId, Receipt receiptData) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) return Optional.empty();

        receiptData.setAppointment(appointmentOpt.get());

        if (receiptData.getTotalAmount() == null) {
            Double total = appointmentOpt.get().getTotalPrice();
            if (total != null) {
                receiptData.setTotalAmount(BigDecimal.valueOf(total));
            }
        }

        return Optional.of(receiptRepository.save(receiptData));
    }

    // Update an existing receipt
    public Optional<Receipt> updateReceipt(Long id, Receipt updated) {
        return receiptRepository.findById(id).map(existing -> {
            existing.setPaymentMethod(updated.getPaymentMethod());
            existing.setNotes(updated.getNotes());
            if (updated.getTotalAmount() != null) {
                existing.setTotalAmount(updated.getTotalAmount());
            }
            return receiptRepository.save(existing);
        });
    }

    // Delete a receipt by its ID
    public boolean deleteReceipt(Long id) {
        if (!receiptRepository.existsById(id)) return false;
        receiptRepository.deleteById(id);
        return true;
    }
}