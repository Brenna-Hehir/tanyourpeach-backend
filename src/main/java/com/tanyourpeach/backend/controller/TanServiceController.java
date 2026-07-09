package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.ServiceCreateRequest;
import com.tanyourpeach.backend.dto.ServiceResponseDto;
import com.tanyourpeach.backend.dto.ServiceUpdateRequest;
import com.tanyourpeach.backend.service.TanServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class TanServiceController {

    private final TanServiceService serviceService;

    public TanServiceController(TanServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceResponseDto> getActiveMainServices() {
        return serviceService.getActiveMainServices();
    }

    @GetMapping("/add-ons")
    public List<ServiceResponseDto> getActiveAddOns() {
        return serviceService.getActiveAddOns();
    }

    @GetMapping("/admin")
    public List<ServiceResponseDto> getAllServicesForAdmin() {
        return serviceService.getAllServicesForAdmin();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ServiceResponseDto> getServiceBySlug(@PathVariable String slug) {
        return serviceService.getActiveServiceBySlug(slug)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> getServiceById(@PathVariable Long id) {
        return serviceService.getActiveServiceById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));
    }

    @PostMapping
    public ResponseEntity<ServiceResponseDto> createService(@Valid @RequestBody ServiceCreateRequest request) {
        ServiceResponseDto created = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceUpdateRequest request
    ) {
        return serviceService.updateService(id, request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service not found"
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        boolean deactivated = serviceService.deactivateService(id);

        if (!deactivated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/force")
    public ResponseEntity<Void> deleteServicePermanently(@PathVariable Long id) {
        boolean deleted = serviceService.deleteServicePermanently(id);

        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
        }

        return ResponseEntity.noContent().build();
    }
}
