package com.tanyourpeach.backend.dto;

import com.tanyourpeach.backend.model.ServiceType;
import com.tanyourpeach.backend.model.TanService;

import java.time.LocalDateTime;

public class ServiceResponseDto {

    private Long serviceId;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private Double basePrice;
    private Integer durationMinutes;
    private ServiceType serviceType;
    private String cardImageUrl;
    private String heroImageUrl;
    private Integer displayOrder;
    private Double rinseTimeMinHours;
    private Double rinseTimeMaxHours;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServiceResponseDto(TanService service) {
        this.serviceId = service.getServiceId();
        this.name = service.getName();
        this.slug = service.getSlug();
        this.shortDescription = service.getShortDescription();
        this.description = service.getDescription();
        this.basePrice = service.getBasePrice();
        this.durationMinutes = service.getDurationMinutes();
        this.serviceType = service.getServiceType();
        this.cardImageUrl = service.getCardImageUrl();
        this.heroImageUrl = service.getHeroImageUrl();
        this.displayOrder = service.getDisplayOrder();
        this.rinseTimeMinHours = service.getRinseTimeMinHours();
        this.rinseTimeMaxHours = service.getRinseTimeMaxHours();
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

    public String getSlug() {
        return slug;
    }

    public String getShortDescription() {
        return shortDescription;
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

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Double getRinseTimeMinHours() {
        return rinseTimeMinHours;
    }

    public Double getRinseTimeMaxHours() {
        return rinseTimeMaxHours;
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
