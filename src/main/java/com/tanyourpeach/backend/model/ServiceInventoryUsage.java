package com.tanyourpeach.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "service_inventory_usage")
public class ServiceInventoryUsage {

    @EmbeddedId
    private ServiceInventoryUsageKey id;

    @ManyToOne
    @MapsId("serviceId")
    @JoinColumn(name = "service_id")
    private TanService service;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Inventory item;

    private int quantityUsed = 1;

    public ServiceInventoryUsageKey getId() {
        return id;
    }

    public void setId(ServiceInventoryUsageKey id) {
        this.id = id;
    }

    public TanService getService() {
        return service;
    }

    public void setService(TanService service) {
        this.service = service;
    }

    public Inventory getItem() {
        return item;
    }

    public void setItem(Inventory item) {
        this.item = item;
    }

    public int getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(int quantityUsed) {
        this.quantityUsed = quantityUsed;
    }
}