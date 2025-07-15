package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.FinancialLog;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.repository.FinancialLogRepository;
import com.tanyourpeach.backend.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private FinancialLogRepository financialLogRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory item;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        item = new Inventory();
        item.setItemId(1L);
        item.setItemName("Gloves");
        item.setQuantity(10);
        item.setUnitCost(BigDecimal.valueOf(2.00));
        item.setTotalSpent(BigDecimal.valueOf(20.00));
    }

    @Test
    void getAllInventory_shouldReturnList() {
        when(inventoryRepository.findAll()).thenReturn(List.of(item));
        List<Inventory> result = inventoryService.getAllInventory();
        assertEquals(1, result.size());
    }

    @Test
    void getInventoryById_shouldReturnItem() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        Optional<Inventory> result = inventoryService.getInventoryById(1L);
        assertTrue(result.isPresent());
        assertEquals("Gloves", result.get().getItemName());
    }

    @Test
    void getInventoryById_shouldReturnEmpty_whenItemNotFound() {
        when(inventoryRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Inventory> result = inventoryService.getInventoryById(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createInventory_shouldSetDefaultsAndSave() {
        Inventory newItem = new Inventory();
        newItem.setItemName("Caps");
        newItem.setQuantity(-5);

        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Inventory saved = inventoryService.createInventory(newItem);
        assertEquals(0, saved.getQuantity());
        assertEquals(BigDecimal.ZERO, saved.getTotalSpent());
    }

    @Test
    void updateInventory_shouldLogExpense_whenQuantityIncreases() {
        Inventory updated = new Inventory();
        updated.setItemName("Gloves");
        updated.setQuantity(15);
        updated.setUnitCost(BigDecimal.valueOf(2.00));
        updated.setNotes("Restocked");
        updated.setLowStockThreshold(5);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Inventory> result = inventoryService.updateInventory(1L, updated);

        assertTrue(result.isPresent());
        verify(financialLogRepository).save(any(FinancialLog.class));
    }

    @Test
    void updateInventory_shouldNotLogExpense_whenQuantityDoesNotIncrease() {
        Inventory updated = new Inventory();
        updated.setItemName("Gloves");
        updated.setQuantity(5);
        updated.setTotalSpent(BigDecimal.valueOf(30));

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        inventoryService.updateInventory(1L, updated);

        verify(financialLogRepository, never()).save(any());
    }

    @Test
    void updateInventory_shouldReturnEmpty_whenItemNotFound() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Inventory> result = inventoryService.updateInventory(99L, new Inventory());

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteInventory_shouldLogAndDelete_whenValid() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        boolean result = inventoryService.deleteInventory(1L);

        assertTrue(result);
        verify(financialLogRepository).save(any(FinancialLog.class));
        verify(inventoryRepository).deleteById(1L);
    }

    @Test
    void deleteInventory_shouldFail_whenNotFound() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = inventoryService.deleteInventory(99L);
        assertFalse(result);
    }

    @Test
    void deductQuantity_shouldSucceed_whenEnoughStock() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean result = inventoryService.deductQuantity(1L, 5);

        assertTrue(result);
        assertEquals(5, item.getQuantity());
    }

    @Test
    void deductQuantity_shouldFail_whenNotEnoughStock() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        boolean result = inventoryService.deductQuantity(1L, 20);
        assertFalse(result);
    }

    @Test
    void deductQuantity_shouldFail_whenItemNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = inventoryService.deductQuantity(999L, 5);

        assertFalse(result);
    }

    @Test
    void addQuantityAndCost_shouldSucceed_whenValid() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean result = inventoryService.addQuantityAndCost(1L, 5, BigDecimal.valueOf(2.00));

        assertTrue(result);
        assertEquals(15, item.getQuantity());
        assertEquals(BigDecimal.valueOf(30.00), item.getTotalSpent());
    }

    @Test
    void addQuantityAndCost_shouldFail_whenInvalid() {
        boolean result = inventoryService.addQuantityAndCost(1L, 0, null);
        assertFalse(result);
    }

    @Test
    void addQuantityAndCost_shouldFail_whenItemNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = inventoryService.addQuantityAndCost(999L, 5, BigDecimal.valueOf(2.00));

        assertFalse(result);
    }
}