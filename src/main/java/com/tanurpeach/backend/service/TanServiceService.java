package com.tanurpeach.backend.service;

import com.tanurpeach.backend.model.TanService;
import com.tanurpeach.backend.repository.TanServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return serviceRepository.save(service);
    }

    // PUT update service
    public Optional<TanService> updateService(Long id, TanService updated) {
        return serviceRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setBasePrice(updated.getBasePrice());
            existing.setDurationMinutes(updated.getDurationMinutes());
            existing.setIsActive(updated.getIsActive());
            return serviceRepository.save(existing);
        });
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