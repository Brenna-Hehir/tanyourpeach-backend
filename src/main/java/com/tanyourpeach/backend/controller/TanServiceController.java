package com.tanyourpeach.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.service.TanServiceService;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
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
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create a new service
    @PostMapping
    public ResponseEntity<TanService> createService(@RequestBody TanService service) {
        TanService created = serviceService.createService(service);
        return created != null
            ? ResponseEntity.status(HttpStatus.CREATED).body(created)
            : ResponseEntity.badRequest().build();
    }

    // PUT update an existing service
    @PutMapping("/{id}")
    public ResponseEntity<TanService> updateService(@PathVariable Long id, @RequestBody TanService updated) {
        return serviceService.updateService(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE service (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        return serviceService.deactivateService(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // DELETE permanently
    @DeleteMapping("/{id}/force")
    public ResponseEntity<Void> deleteServicePermanently(@PathVariable Long id) {
        return serviceService.deleteServicePermanently(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}