package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private FinancialLogRepository financialLogRepository;

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
        // Basic validation
        if (inventory.getItemName() == null || inventory.getItemName().isBlank()) return null;
        if (inventory.getUnitCost() != null && inventory.getUnitCost().compareTo(BigDecimal.ZERO) < 0) return null;

        if (inventory.getQuantity() == null || inventory.getQuantity() < 0) inventory.setQuantity(0);
        if (inventory.getTotalSpent() == null) inventory.setTotalSpent(BigDecimal.ZERO);

        return inventoryRepository.save(inventory);
    }


    // Update existing inventory item with expense logging
    public Optional<Inventory> updateInventory(Long id, Inventory updated) {
        // Basic validation
        if (updated.getItemName() == null || updated.getItemName().isBlank()) return Optional.empty();
        if (updated.getUnitCost() != null && updated.getUnitCost().compareTo(BigDecimal.ZERO) < 0) return Optional.empty();

        return inventoryRepository.findById(id).map(existing -> {
            int oldQuantity = existing.getQuantity() != null ? existing.getQuantity() : 0;
            BigDecimal oldTotalSpent = existing.getTotalSpent() != null ? existing.getTotalSpent() : BigDecimal.ZERO;

            existing.setItemName(updated.getItemName());
            existing.setQuantity(updated.getQuantity());
            existing.setUnitCost(updated.getUnitCost());
            existing.setNotes(updated.getNotes());
            existing.setLowStockThreshold(updated.getLowStockThreshold());

            // Auto-log expense if more stock was added and unit cost is available
            if (updated.getQuantity() > oldQuantity && updated.getUnitCost() != null) {
                int addedQty = updated.getQuantity() - oldQuantity;
                BigDecimal addedCost = updated.getUnitCost().multiply(BigDecimal.valueOf(addedQty));
                existing.setTotalSpent(oldTotalSpent.add(addedCost));

                FinancialLog log = new FinancialLog();
                log.setType(FinancialLog.Type.expense);
                log.setSource("inventory");
                log.setReferenceId(existing.getItemId());
                log.setAmount(addedCost);
                log.setDescription("Added " + addedQty + " units of " + existing.getItemName());
                financialLogRepository.save(log);
            } else {
                existing.setTotalSpent(updated.getTotalSpent()); // maintain current manual edit behavior
            }

            return inventoryRepository.save(existing);
        });
    }

    // Delete inventory item with logging
    public boolean deleteInventory(Long id) {
        Optional<Inventory> optional = inventoryRepository.findById(id);
        if (optional.isEmpty()) return false;

        Inventory item = optional.get();
        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        BigDecimal totalSpent = item.getTotalSpent() != null ? item.getTotalSpent() : BigDecimal.ZERO;

        // Only log if there's meaningful value to track
        if (quantity > 0 && totalSpent.compareTo(BigDecimal.ZERO) > 0) {
            FinancialLog log = new FinancialLog();
            log.setType(FinancialLog.Type.expense);
            log.setSource("inventory");
            log.setReferenceId(item.getItemId());
            log.setAmount(totalSpent);
            log.setDescription("Deleted inventory item '" + item.getItemName() + "' with " + quantity + " units remaining");
            financialLogRepository.save(log);
        }

        inventoryRepository.deleteById(id);
        return true;
    }

    // Deduct quantity from inventory item
    public boolean deductQuantity(Long itemId, int amount) {
        if (amount <= 0) return false;
        
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