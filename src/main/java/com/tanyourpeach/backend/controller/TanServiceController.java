package com.tanyourpeach.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.service.TanServiceService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class TanServiceController {

    @Autowired
    private TanServiceService serviceService;

    // GET all services
    @GetMapping
    public List<TanService> getAllServices() {
        return serviceService.getAllServices();
    }

    // GET single service by ID
    @GetMapping("/{id}")
    public ResponseEntity<TanService> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));
    }

    // POST create a new service
    @PostMapping
    public ResponseEntity<TanService> createService(@Valid @RequestBody TanService service) {
        TanService created = serviceService.createService(service);
        if (created == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create service");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT update an existing service
    @PutMapping("/{id}")
    public ResponseEntity<TanService> updateService(@PathVariable Long id, @Valid @RequestBody TanService updated) {
        return serviceService.updateService(id, updated)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));
    }

    // DELETE service (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        boolean deactivated = serviceService.deactivateService(id);
        if (!deactivated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
        }

        return ResponseEntity.noContent().build();
    }

    // DELETE permanently
    @DeleteMapping("/{id}/force")
    public ResponseEntity<Void> deleteServicePermanently(@PathVariable Long id) {
        boolean deleted = serviceService.deleteServicePermanently(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
        }

        return ResponseEntity.noContent().build();
    }
}