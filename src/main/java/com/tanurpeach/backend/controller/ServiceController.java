package com.tanurpeach.backend.controller;

import com.tanurpeach.backend.model.Service;
import com.tanurpeach.backend.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    //GET all services
    @GetMapping
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    //GET single service by ID
    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //POST create a new service
    @PostMapping
    public Service createService(@RequestBody Service service) {
        return serviceRepository.save(service);
    }

    //PUT update an existing service
    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(@PathVariable Long id, @RequestBody Service updated) {
        return serviceRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDescription(updated.getDescription());
                    existing.setBasePrice(updated.getBasePrice());
                    existing.setDurationMinutes(updated.getDurationMinutes());
                    existing.setIsActive(updated.getIsActive());
                    return ResponseEntity.ok(serviceRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE service (soft delete by disabling)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        Optional<Service> optionalService = serviceRepository.findById(id);
        if (optionalService.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Service service = optionalService.get();
        service.setIsActive(false);
        serviceRepository.save(service);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/force")
    public ResponseEntity<Void> deleteServicePermanently(@PathVariable Long id) {
        if (!serviceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}