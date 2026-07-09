package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.ServiceCreateRequest;
import com.tanyourpeach.backend.dto.ServiceResponseDto;
import com.tanyourpeach.backend.dto.ServiceUpdateRequest;
import com.tanyourpeach.backend.model.ServiceType;
import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.repository.TanServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class TanServiceService {

    private final TanServiceRepository serviceRepository;

    public TanServiceService(TanServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceResponseDto> getActiveMainServices() {
        return serviceRepository
                .findByIsActiveTrueAndServiceTypeOrderByDisplayOrderAscNameAsc(ServiceType.MAIN_SERVICE)
                .stream()
                .map(ServiceResponseDto::new)
                .toList();
    }

    public List<ServiceResponseDto> getActiveAddOns() {
        return serviceRepository
                .findByIsActiveTrueAndServiceTypeOrderByDisplayOrderAscNameAsc(ServiceType.ADD_ON)
                .stream()
                .map(ServiceResponseDto::new)
                .toList();
    }

    public List<ServiceResponseDto> getAllServicesForAdmin() {
        return serviceRepository
                .findAllByOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(ServiceResponseDto::new)
                .toList();
    }

    public Optional<ServiceResponseDto> getActiveServiceById(Long id) {
        return serviceRepository
                .findByServiceIdAndIsActiveTrue(id)
                .map(ServiceResponseDto::new);
    }

    public Optional<ServiceResponseDto> getActiveServiceBySlug(String slug) {
        return serviceRepository
                .findBySlugAndIsActiveTrue(slug)
                .map(ServiceResponseDto::new);
    }

    public ServiceResponseDto createService(ServiceCreateRequest request) {
        validateSlugAvailableForCreate(request.getSlug());

        TanService service = new TanService();
        applyRequest(service, request);

        return new ServiceResponseDto(serviceRepository.save(service));
    }

    public Optional<ServiceResponseDto> updateService(Long id, ServiceUpdateRequest request) {
        Optional<TanService> existingOpt = serviceRepository.findById(id);

        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        validateSlugAvailableForUpdate(request.getSlug(), id);

        TanService existing = existingOpt.get();
        applyRequest(existing, request);

        return Optional.of(new ServiceResponseDto(serviceRepository.save(existing)));
    }

    public boolean deactivateService(Long id) {
        Optional<TanService> optional = serviceRepository.findById(id);

        if (optional.isEmpty()) {
            return false;
        }

        TanService service = optional.get();
        service.setIsActive(false);
        serviceRepository.save(service);
        return true;
    }

    public boolean deleteServicePermanently(Long id) {
        if (!serviceRepository.existsById(id)) {
            return false;
        }

        serviceRepository.deleteById(id);
        return true;
    }

    private void applyRequest(TanService service, ServiceCreateRequest request) {
        service.setName(request.getName());
        service.setSlug(request.getSlug());
        service.setShortDescription(request.getShortDescription());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setServiceType(defaultServiceType(request.getServiceType()));
        service.setCardImageUrl(request.getCardImageUrl());
        service.setHeroImageUrl(request.getHeroImageUrl());
        service.setDisplayOrder(defaultDisplayOrder(request.getDisplayOrder()));
        service.setRinseTimeMinHours(request.getRinseTimeMinHours());
        service.setRinseTimeMaxHours(request.getRinseTimeMaxHours());
        service.setIsActive(defaultIsActive(request.getIsActive()));
    }

    private ServiceType defaultServiceType(ServiceType serviceType) {
        return serviceType == null ? ServiceType.MAIN_SERVICE : serviceType;
    }

    private Integer defaultDisplayOrder(Integer displayOrder) {
        return displayOrder == null ? 0 : displayOrder;
    }

    private Boolean defaultIsActive(Boolean isActive) {
        return isActive == null ? true : isActive;
    }

    private void validateSlugAvailableForCreate(String slug) {
        if (slug != null && serviceRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service slug already exists");
        }
    }

    private void validateSlugAvailableForUpdate(String slug, Long serviceId) {
        if (slug != null && serviceRepository.existsBySlugAndServiceIdNot(slug, serviceId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service slug already exists");
        }
    }
}
