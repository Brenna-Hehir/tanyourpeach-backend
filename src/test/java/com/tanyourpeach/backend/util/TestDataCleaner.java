package com.tanyourpeach.backend.util;

import com.tanyourpeach.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDataCleaner {

    @Autowired private AppointmentStatusHistoryRepository appointmentStatusHistoryRepository;
    @Autowired private ReceiptRepository receiptRepository;
    @Autowired private FinancialLogRepository financialLogRepository;
    @Autowired private ServiceInventoryUsageRepository serviceInventoryUsageRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private TanServiceRepository tanServiceRepository;
    @Autowired private UserRepository userRepository;

    public void cleanAll() {
        appointmentStatusHistoryRepository.deleteAll();
        receiptRepository.deleteAll();
        financialLogRepository.deleteAll();
        serviceInventoryUsageRepository.deleteAll();
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        inventoryRepository.deleteAll();
        tanServiceRepository.deleteAll();
        userRepository.deleteAll();
    }
}