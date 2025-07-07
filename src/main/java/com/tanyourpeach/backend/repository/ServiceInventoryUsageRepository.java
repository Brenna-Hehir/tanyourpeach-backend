package com.tanyourpeach.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tanyourpeach.backend.model.ServiceInventoryUsage;
import com.tanyourpeach.backend.model.ServiceInventoryUsageKey;

import java.util.List;

public interface ServiceInventoryUsageRepository extends JpaRepository<ServiceInventoryUsage, ServiceInventoryUsageKey> {
    // Find all usage records for a specific service
    List<ServiceInventoryUsage> findByService_ServiceId(Long serviceId);
    // Find all usage records for a specific inventory item
    List<ServiceInventoryUsage> findByItem_ItemId(Long itemId);
}