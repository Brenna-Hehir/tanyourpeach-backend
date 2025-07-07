package com.tanurpeach.backend.service;

import com.tanurpeach.backend.model.Inventory;
import com.tanurpeach.backend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    // GET all inventory items
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // GET inventory item by ID
    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    // POST create new inventory item
    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    // PUT update inventory item
    public Optional<Inventory> updateInventory(Long id, Inventory updated) {
        return inventoryRepository.findById(id).map(existing -> {
            existing.setItemName(updated.getItemName());
            existing.setQuantity(updated.getQuantity());
            existing.setUnitCost(updated.getUnitCost());
            return inventoryRepository.save(existing);
        });
    }

    // DELETE inventory item
    public boolean deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) return false;
        inventoryRepository.deleteById(id);
        return true;
    }

    // PUT to deduct quantity from inventory item
    public boolean deductQuantity(Long itemId, int amount) {
        Optional<Inventory> optional = inventoryRepository.findById(itemId);
        if (optional.isEmpty()) return false;

        Inventory item = optional.get();
        if (item.getQuantity() < amount) return false;

        item.setQuantity(item.getQuantity() - amount);
        inventoryRepository.save(item);
        return true;
    }
}