package com.tanyourpeach.backend.dto;

import com.tanyourpeach.backend.model.TanService;

import java.time.LocalDateTime;

public class ServiceResponseDto {

    private Long serviceId;
    private String name;
    private String description;
    private Double basePrice;
    private Integer durationMinutes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServiceResponseDto(TanService service) {
        this.serviceId = service.getServiceId();
        this.name = service.getName();
        this.description = service.getDescription();
        this.basePrice = service.getBasePrice();
        this.durationMinutes = service.getDurationMinutes();
        this.isActive = service.getIsActive();
        this.createdAt = service.getCreatedAt();
        this.updatedAt = service.getUpdatedAt();
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}