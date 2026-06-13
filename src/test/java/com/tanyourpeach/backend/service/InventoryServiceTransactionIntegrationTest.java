package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.util.TestDataCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class InventoryServiceTransactionIntegrationTest {

    @Autowired private InventoryService inventoryService;
    @Autowired private TestDataCleaner testDataCleaner;

    @MockitoSpyBean
    private InventoryRepository inventoryRepository;

    @Autowired
    private FinancialLogRepository financialLogRepository;

    @BeforeEach
    void setUp() {
        testDataCleaner.cleanAll();
    }

    @Test
    void updateInventory_shouldRollbackFinancialLog_whenInventorySaveFails() {
        Inventory existing = saveInventory("Rollback Gloves", 10, "20.00");

        Inventory updated = new Inventory();
        updated.setItemName("Rollback Gloves");
        updated.setQuantity(15);
        updated.setUnitCost(new BigDecimal("2.00"));
        updated.setNotes("Restocked");
        updated.setLowStockThreshold(2);

        doThrow(new RuntimeException("forced inventory save failure"))
                .when(inventoryRepository)
                .save(any(Inventory.class));

        assertThrows(RuntimeException.class,
                () -> inventoryService.updateInventory(existing.getItemId(), updated));

        Inventory reloadedInventory =
                inventoryRepository.findById(existing.getItemId()).orElseThrow();

        assertEquals(10, reloadedInventory.getQuantity());
        assertEquals(new BigDecimal("20.00"), reloadedInventory.getTotalSpent());
        assertTrue(financialLogRepository.findAll().isEmpty());
    }

    @Test
    void deleteInventory_shouldRollbackFinancialLog_whenInventoryDeleteFails() {
        Inventory existing = saveInventory("Rollback Solution", 10, "125.00");

        doThrow(new RuntimeException("forced inventory delete failure"))
                .when(inventoryRepository)
                .deleteById(existing.getItemId());

        assertThrows(RuntimeException.class,
                () -> inventoryService.deleteInventory(existing.getItemId()));

        assertTrue(inventoryRepository.findById(existing.getItemId()).isPresent());
        assertTrue(financialLogRepository.findAll().isEmpty());
    }

    private Inventory saveInventory(String itemName, int quantity, String totalSpent) {
        Inventory inventory = new Inventory();
        inventory.setItemName(itemName);
        inventory.setQuantity(quantity);
        inventory.setUnitCost(new BigDecimal("2.00"));
        inventory.setTotalSpent(new BigDecimal(totalSpent));
        inventory.setLowStockThreshold(1);
        return inventoryRepository.save(inventory);
    }
}