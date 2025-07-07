package com.tanyourpeach.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.repository.InventoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    // Get all inventory items
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // Get inventory item by ID
    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    // Create new inventory item
    public Inventory createInventory(Inventory inventory) {
        if (inventory.getQuantity() < 0) inventory.setQuantity(0);
        if (inventory.getTotalSpent() == null) inventory.setTotalSpent(BigDecimal.ZERO);
        return inventoryRepository.save(inventory);
    }

    // Update existing inventory item
    public Optional<Inventory> updateInventory(Long id, Inventory updated) {
        return inventoryRepository.findById(id).map(existing -> {
            existing.setItemName(updated.getItemName());
            existing.setQuantity(updated.getQuantity());
            existing.setUnitCost(updated.getUnitCost());
            existing.setTotalSpent(updated.getTotalSpent());
            existing.setNotes(updated.getNotes());
            return inventoryRepository.save(existing);
        });
    }

    // Delete inventory item
    public boolean deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) return false;
        inventoryRepository.deleteById(id);
        return true;
    }

    // Deduct quantity from inventory item
    public boolean deductQuantity(Long itemId, int amount) {
        Optional<Inventory> optional = inventoryRepository.findById(itemId);
        if (optional.isEmpty()) return false;

        Inventory item = optional.get();
        if (item.getQuantity() < amount) return false;

        item.setQuantity(item.getQuantity() - amount);
        return inventoryRepository.save(item) != null;
    }

    // Add quantity and cost to inventory item
    public boolean addQuantityAndCost(Long itemId, int addedQty, BigDecimal costPerUnit) {
        Optional<Inventory> optional = inventoryRepository.findById(itemId);
        if (optional.isEmpty() || addedQty <= 0 || costPerUnit == null) return false;

        Inventory item = optional.get();
        item.setQuantity(item.getQuantity() + addedQty);

        BigDecimal addedCost = costPerUnit.multiply(BigDecimal.valueOf(addedQty));
        item.setTotalSpent(item.getTotalSpent().add(addedCost));

        return inventoryRepository.save(item) != null;
    }
}