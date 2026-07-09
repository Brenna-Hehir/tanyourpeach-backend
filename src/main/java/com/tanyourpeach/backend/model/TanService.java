package com.tanyourpeach.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "services")
public class TanService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must be under 100 characters")
    private String name;

    @Size(max = 150, message = "Slug must be under 150 characters")
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug must use lowercase letters, numbers, and hyphens only"
    )
    @Column(unique = true)
    private String slug;

    @Size(max = 255, message = "Short description must be under 255 characters")
    @Column(name = "short_description")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price cannot be negative")
    private Double basePrice;

    @NotNull(message = "Duration is required")
    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationMinutes;

    @NotNull(message = "Service type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType = ServiceType.MAIN_SERVICE;

    @Size(max = 500, message = "Card image URL must be under 500 characters")
    @Column(name = "card_image_url")
    private String cardImageUrl;

    @Size(max = 500, message = "Hero image URL must be under 500 characters")
    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order cannot be negative")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum rinse time must be greater than 0")
    @Column(name = "rinse_time_min_hours")
    private Double rinseTimeMinHours;

    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum rinse time must be greater than 0")
    @Column(name = "rinse_time_max_hours")
    private Double rinseTimeMaxHours;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (serviceType == null) {
            serviceType = ServiceType.MAIN_SERVICE;
        }

        if (displayOrder == null) {
            displayOrder = 0;
        }

        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (serviceType == null) {
            serviceType = ServiceType.MAIN_SERVICE;
        }

        if (displayOrder == null) {
            displayOrder = 0;
        }

        if (isActive == null) {
            isActive = true;
        }
    }

    @JsonIgnore
    @AssertTrue(message = "Main services must have a base price greater than 0")
    public boolean isMainServiceBasePriceValid() {
        if (serviceType != ServiceType.MAIN_SERVICE) {
            return true;
        }

        return basePrice != null && basePrice > 0;
    }

    @JsonIgnore
    @AssertTrue(message = "Main services must have a duration greater than 0")
    public boolean isMainServiceDurationValid() {
        if (serviceType != ServiceType.MAIN_SERVICE) {
            return true;
        }

        return durationMinutes != null && durationMinutes > 0;
    }

    @JsonIgnore
    @AssertTrue(message = "Short description cannot be blank when provided")
    public boolean isShortDescriptionValid() {
        return shortDescription == null || !shortDescription.trim().isEmpty();
    }

    @JsonIgnore
    @AssertTrue(message = "Card image URL cannot be blank when provided")
    public boolean isCardImageUrlValid() {
        return cardImageUrl == null || !cardImageUrl.trim().isEmpty();
    }

    @JsonIgnore
    @AssertTrue(message = "Hero image URL cannot be blank when provided")
    public boolean isHeroImageUrlValid() {
        return heroImageUrl == null || !heroImageUrl.trim().isEmpty();
    }

    @JsonIgnore
    @AssertTrue(message = "Rinse time min and max must both be provided together")
    public boolean isRinseTimePairValid() {
        return (rinseTimeMinHours == null && rinseTimeMaxHours == null)
                || (rinseTimeMinHours != null && rinseTimeMaxHours != null);
    }

    @JsonIgnore
    @AssertTrue(message = "Rinse time max must be greater than or equal to rinse time min")
    public boolean isRinseTimeRangeValid() {
        if (rinseTimeMinHours == null || rinseTimeMaxHours == null) {
            return true;
        }

        return rinseTimeMaxHours >= rinseTimeMinHours;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public void setHeroImageUrl(String heroImageUrl) {
        this.heroImageUrl = heroImageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Double getRinseTimeMinHours() {
        return rinseTimeMinHours;
    }

    public void setRinseTimeMinHours(Double rinseTimeMinHours) {
        this.rinseTimeMinHours = rinseTimeMinHours;
    }

    public Double getRinseTimeMaxHours() {
        return rinseTimeMaxHours;
    }

    public void setRinseTimeMaxHours(Double rinseTimeMaxHours) {
        this.rinseTimeMaxHours = rinseTimeMaxHours;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}