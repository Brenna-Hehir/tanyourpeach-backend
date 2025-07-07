package com.tanurpeach.backend.repository;

import com.tanurpeach.backend.model.ServiceInventoryUsage;
import com.tanurpeach.backend.model.ServiceInventoryUsageKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceInventoryUsageRepository extends JpaRepository<ServiceInventoryUsage, ServiceInventoryUsageKey> {
    // Find all usage records for a specific service
    List<ServiceInventoryUsage> findByService_ServiceId(Long serviceId);
    // Find all usage records for a specific inventory item
    List<ServiceInventoryUsage> findByItem_ItemId(Long itemId);
}