package com.tanyourpeach.backend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ServiceInventoryUsageKey implements Serializable {

    private Long serviceId;
    
    private Long itemId;

    public ServiceInventoryUsageKey() {}

    // Constructor with parameters
    public ServiceInventoryUsageKey(Long serviceId, Long itemId) {
        this.serviceId = serviceId;
        this.itemId = itemId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    // Override equals and hashCode for proper comparison in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceInventoryUsageKey)) return false;
        ServiceInventoryUsageKey that = (ServiceInventoryUsageKey) o;
        return Objects.equals(serviceId, that.serviceId) &&
               Objects.equals(itemId, that.itemId);
    }

    // Ensure that the hashCode method is consistent with equals
    @Override
    public int hashCode() {
        return Objects.hash(serviceId, itemId);
    }
}