package com.tanurpeach.backend.controller;

import com.tanurpeach.backend.model.ServiceInventoryUsage;
import com.tanurpeach.backend.service.ServiceInventoryUsageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-usage")
@CrossOrigin(origins = "*")
public class ServiceInventoryUsageController {

    @Autowired
    private ServiceInventoryUsageService usageService;

    // GET all links
    @GetMapping
    public List<ServiceInventoryUsage> getAllUsages() {
        return usageService.getAllUsages();
    }

    // GET all items for a service
    @GetMapping("/service/{serviceId}")
    public List<ServiceInventoryUsage> getByService(@PathVariable Long serviceId) {
        return usageService.getByServiceId(serviceId);
    }

    // GET all services using an inventory item
    @GetMapping("/item/{itemId}")
    public List<ServiceInventoryUsage> getByItem(@PathVariable Long itemId) {
        return usageService.getByItemUsageId(itemId);
    }

    // POST create a link
    @PostMapping
    public ResponseEntity<ServiceInventoryUsage> createUsage(@RequestBody ServiceInventoryUsage usage) {
        return usageService.createUsage(usage)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // DELETE a link
    @DeleteMapping("/{serviceId}/{itemId}")
    public ResponseEntity<Void> deleteUsage(@PathVariable Long serviceId, @PathVariable Long itemId) {
        return usageService.deleteUsage(serviceId, itemId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}