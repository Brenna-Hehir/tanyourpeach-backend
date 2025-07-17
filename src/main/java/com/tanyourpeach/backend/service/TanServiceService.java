package com.tanyourpeach.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.repository.TanServiceRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TanServiceService {

    @Autowired
    private TanServiceRepository serviceRepository;

    // GET all services
    public List<TanService> getAllServices() {
        return serviceRepository.findAll();
    }

    // GET service by ID
    public Optional<TanService> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    // POST create new service
    public TanService createService(TanService service) {
        if (service.getName() == null || service.getName().trim().isEmpty()) return null;
        if (service.getBasePrice() == null || service.getBasePrice() <= 0) return null;
        if (service.getDurationMinutes() == null || service.getDurationMinutes() <= 0) return null;

        return serviceRepository.save(service);
    }

    // PUT update service
    public Optional<TanService> updateService(Long id, TanService updated) {
        Optional<TanService> existingOpt = serviceRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        if (updated.getName() == null || updated.getName().trim().isEmpty()) return Optional.empty();
        if (updated.getBasePrice() == null || updated.getBasePrice() <= 0) return Optional.empty();
        if (updated.getDurationMinutes() == null || updated.getDurationMinutes() <= 0) return Optional.empty();

        TanService existing = existingOpt.get();
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setBasePrice(updated.getBasePrice());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setIsActive(updated.getIsActive());

        return Optional.of(serviceRepository.save(existing));
    }

    // PUT deactivate service (soft delete)
    public boolean deactivateService(Long id) {
        Optional<TanService> optional = serviceRepository.findById(id);
        if (optional.isEmpty()) return false;

        TanService service = optional.get();
        service.setIsActive(false);
        serviceRepository.save(service);
        return true;
    }

    // DELETE service permanently
    public boolean deleteServicePermanently(Long id) {
        if (!serviceRepository.existsById(id)) return false;
        serviceRepository.deleteById(id);
        return true;
    }
}