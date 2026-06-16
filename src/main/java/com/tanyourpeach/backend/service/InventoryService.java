package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Inventory createInventory(Inventory inventory) {
        if (inventory.getItemName() == null || inventory.getItemName().isBlank()) return null;
        if (inventory.getUnitCost() != null && inventory.getUnitCost().compareTo(BigDecimal.ZERO) < 0) return null;

        int startingQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        if (startingQuantity < 0) return null;

        BigDecimal unitCost = inventory.getUnitCost() != null ? inventory.getUnitCost() : BigDecimal.ZERO;
        BigDecimal startingTotalSpent = unitCost.multiply(BigDecimal.valueOf(startingQuantity));

        inventory.setQuantity(startingQuantity);
        inventory.setTotalSpent(startingTotalSpent);

        Inventory saved = inventoryRepository.save(inventory);

        if (startingQuantity > 0 && startingTotalSpent.compareTo(BigDecimal.ZERO) > 0) {
            FinancialLog log = new FinancialLog();
            log.setType(FinancialLog.Type.expense);
            log.setSource("inventory");
            log.setReferenceId(saved.getItemId());
            log.setAmount(startingTotalSpent);
            log.setDescription("Initial inventory purchase: " + startingQuantity + " units of " + saved.getItemName());
            financialLogRepository.save(log);
        }

        return saved;
    }


    // Update existing inventory item metadata only
    public Optional<Inventory> updateInventory(Long id, Inventory updated) {
        if (updated.getItemName() == null || updated.getItemName().isBlank()) return Optional.empty();
        if (updated.getUnitCost() != null && updated.getUnitCost().compareTo(BigDecimal.ZERO) < 0) return Optional.empty();

        return inventoryRepository.findById(id).map(existing -> {
            existing.setItemName(updated.getItemName());
            existing.setUnitCost(updated.getUnitCost());
            existing.setNotes(updated.getNotes());
            existing.setLowStockThreshold(updated.getLowStockThreshold());

            return inventoryRepository.save(existing);
        });
    }

    // Delete inventory item with logging
    @Transactional
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

    // Add purchased stock to inventory item
    @Transactional
    public boolean addQuantityAndCost(Long itemId, int addedQty, BigDecimal costPerUnit) {
        Optional<Inventory> optional = inventoryRepository.findById(itemId);
        if (optional.isEmpty() || addedQty <= 0 || costPerUnit == null) return false;
        if (costPerUnit.compareTo(BigDecimal.ZERO) <= 0) return false;

        Inventory item = optional.get();

        int currentQuantity = item.getQuantity() != null ? item.getQuantity() : 0;
        BigDecimal currentTotalSpent = item.getTotalSpent() != null
                ? item.getTotalSpent()
                : BigDecimal.ZERO;

        BigDecimal addedCost = costPerUnit.multiply(BigDecimal.valueOf(addedQty));

        item.setQuantity(currentQuantity + addedQty);
        item.setTotalSpent(currentTotalSpent.add(addedCost));

        Inventory saved = inventoryRepository.save(item);

        FinancialLog log = new FinancialLog();
        log.setType(FinancialLog.Type.expense);
        log.setSource("inventory");
        log.setReferenceId(saved.getItemId());
        log.setAmount(addedCost);
        log.setDescription("Added " + addedQty + " units of " + saved.getItemName());
        financialLogRepository.save(log);

        return true;
    }

    // Remove stock from inventory item
    @Transactional
    public boolean removeQuantity(Long itemId, int removedQty) {
        if (removedQty <= 0) return false;

        Optional<Inventory> optional = inventoryRepository.findById(itemId);
        if (optional.isEmpty()) return false;

        Inventory item = optional.get();

        int currentQuantity = item.getQuantity() != null ? item.getQuantity() : 0;
        if (currentQuantity < removedQty) return false;

        BigDecimal unitCost = item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO;
        BigDecimal currentTotalSpent = item.getTotalSpent() != null
                ? item.getTotalSpent()
                : BigDecimal.ZERO;

        BigDecimal removedValue = unitCost.multiply(BigDecimal.valueOf(removedQty));

        item.setQuantity(currentQuantity - removedQty);
        item.setTotalSpent(currentTotalSpent.subtract(removedValue).max(BigDecimal.ZERO));

        Inventory saved = inventoryRepository.save(item);

        if (removedValue.compareTo(BigDecimal.ZERO) > 0) {
            FinancialLog log = new FinancialLog();
            log.setType(FinancialLog.Type.expense);
            log.setSource("inventory");
            log.setReferenceId(saved.getItemId());
            log.setAmount(removedValue);
            log.setDescription("Removed " + removedQty + " units of " + saved.getItemName());
            financialLogRepository.save(log);
        }

        return true;
    }
}