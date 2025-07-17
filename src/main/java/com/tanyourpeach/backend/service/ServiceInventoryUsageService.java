package com.tanyourpeach.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.*;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.ServiceInventoryUsageRepository;
import com.tanyourpeach.backend.repository.TanServiceRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceInventoryUsageService {

    @Autowired
    private ServiceInventoryUsageRepository usageRepository;

    @Autowired
    private TanServiceRepository tanServiceRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    // Get all service-inventory usage records
    public List<ServiceInventoryUsage> getAllUsages() {
        return usageRepository.findAll();
    }

    // Get all usage records for a specific service
    public List<ServiceInventoryUsage> getByServiceId(Long serviceId) {
        return usageRepository.findByService_ServiceId(serviceId);
    }

    // Get all usage records for a specific inventory item
    public List<ServiceInventoryUsage> getByItemUsageId(Long itemId) {
        return usageRepository.findByItem_ItemId(itemId);
    }

    // Create a new service-inventory usage record
    public Optional<ServiceInventoryUsage> createUsage(ServiceInventoryUsage usage) {
        if (usage.getService() == null || usage.getItem() == null) return Optional.empty();

        // Defensive check: ensure service and item exist
        Long serviceId = usage.getService().getServiceId();
        Long itemId = usage.getItem().getItemId();

        Optional<TanService> serviceOpt = tanServiceRepository.findById(serviceId);
        Optional<Inventory> itemOpt = inventoryRepository.findById(itemId);

        if (serviceOpt.isEmpty() || itemOpt.isEmpty()) return Optional.empty();
        Integer quantity = usage.getQuantityUsed();
        if (quantity == null || quantity <= 0) {
            usage.setQuantityUsed(1); // Default to 1 if not specified or invalid
        }

        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(serviceId, itemId);
        usage.setId(key);
        usage.setService(serviceOpt.get());
        usage.setItem(itemOpt.get());

        return Optional.of(usageRepository.save(usage));
    }

    // Update the quantity used for a specific service-inventory usage record
    public Optional<ServiceInventoryUsage> updateQuantity(Long serviceId, Long itemId, int newQuantityUsed) {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(serviceId, itemId);
        Optional<ServiceInventoryUsage> usageOpt = usageRepository.findById(key);

        if (usageOpt.isEmpty()) return Optional.empty();
        if (newQuantityUsed <= 0) return Optional.empty(); // Must use positive quantity

        ServiceInventoryUsage usage = usageOpt.get();
        usage.setQuantityUsed(newQuantityUsed);
        return Optional.of(usageRepository.save(usage));
    }

    // Delete a service-inventory usage record
    public boolean deleteUsage(Long serviceId, Long itemId) {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(serviceId, itemId);
        if (!usageRepository.existsById(key)) return false;
        usageRepository.deleteById(key);
        return true;
    }
}