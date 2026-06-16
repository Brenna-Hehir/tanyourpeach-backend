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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
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
    void createInventory_shouldCalculateTotalSpentAndLogInitialPurchase_whenStartingStockProvided() {
        Inventory newItem = new Inventory();
        newItem.setItemName("Caps");
        newItem.setQuantity(10);
        newItem.setUnitCost(BigDecimal.valueOf(8.00));

        when(inventoryRepository.save(any())).thenAnswer(i -> {
            Inventory saved = i.getArgument(0);
            saved.setItemId(2L);
            return saved;
        });

        Inventory saved = inventoryService.createInventory(newItem);

        assertNotNull(saved);
        assertEquals(10, saved.getQuantity());
        assertTrue(saved.getTotalSpent().compareTo(BigDecimal.valueOf(80.00)) == 0);

        verify(financialLogRepository).save(argThat(log ->
                log.getType() == FinancialLog.Type.expense
                        && log.getAmount().compareTo(BigDecimal.valueOf(80.00)) == 0
                        && log.getDescription().contains("Initial inventory purchase")
        ));
    }

    @Test
    void createInventory_shouldDefaultToZero_whenQuantityOrCostIsNull() {
        // Arrange
        Inventory input = new Inventory();
        input.setItemName("Gloves");
        input.setQuantity(null);               // testing null
        input.setTotalSpent(null);            // testing null
        input.setUnitCost(new BigDecimal("1.50"));

        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Inventory result = inventoryService.createInventory(input);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getQuantity());
        assertTrue(result.getTotalSpent().compareTo(BigDecimal.ZERO) == 0);
        assertEquals(new BigDecimal("1.50"), result.getUnitCost());
        verify(inventoryRepository).save(any());
    }

    @Test
    void createInventory_shouldNotLogInitialPurchase_whenQuantityIsZero() {
        Inventory newItem = new Inventory();
        newItem.setItemName("Caps");
        newItem.setQuantity(0);
        newItem.setUnitCost(BigDecimal.valueOf(8.00));

        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Inventory saved = inventoryService.createInventory(newItem);

        assertNotNull(saved);
        assertEquals(0, saved.getQuantity());
        assertTrue(saved.getTotalSpent().compareTo(BigDecimal.ZERO) == 0);

        verify(financialLogRepository, never()).save(any());
    }

    @Test
    void createInventory_shouldRejectBlankItemName() {
        Inventory input = new Inventory();
        input.setItemName("   "); // Invalid name
        input.setQuantity(5);
        input.setUnitCost(BigDecimal.valueOf(1.00));
        input.setTotalSpent(BigDecimal.ZERO);

        Inventory result = inventoryService.createInventory(input);

        assertNull(result); // Should be rejected
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_shouldRejectNegativeQuantity() {
        Inventory newItem = new Inventory();
        newItem.setItemName("Caps");
        newItem.setQuantity(-5);
        newItem.setUnitCost(BigDecimal.valueOf(8.00));

        Inventory saved = inventoryService.createInventory(newItem);

        assertNull(saved);
        verify(inventoryRepository, never()).save(any());
        verify(financialLogRepository, never()).save(any());
    }

    @Test
    void createInventory_shouldRejectNegativeUnitCost() {
        Inventory input = new Inventory();
        input.setItemName("Test Item");
        input.setQuantity(5);
        input.setUnitCost(BigDecimal.valueOf(-1.00)); // Invalid
        input.setTotalSpent(BigDecimal.ZERO);

        Inventory result = inventoryService.createInventory(input);

        assertNull(result); // Should return null
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_shouldFail_whenItemNameIsBlank() {
        Inventory newItem = new Inventory();
        newItem.setItemName("   ");
        newItem.setQuantity(5);

        Inventory result = inventoryService.createInventory(newItem);
        assertNull(result);
    }

    @Test
    void createInventory_shouldFail_whenUnitCostIsNegative() {
        Inventory newItem = new Inventory();
        newItem.setItemName("Spray");
        newItem.setQuantity(5);
        newItem.setUnitCost(BigDecimal.valueOf(-1.00));

        Inventory result = inventoryService.createInventory(newItem);
        assertNull(result);
    }

    @Test
    void updateInventory_shouldOnlyUpdateMetadataAndIgnoreQuantityAndTotalSpent() {
        Inventory updated = new Inventory();
        updated.setItemName("Updated Gloves");
        updated.setQuantity(999);
        updated.setTotalSpent(BigDecimal.valueOf(9999.00));
        updated.setUnitCost(BigDecimal.valueOf(3.00));
        updated.setNotes("Updated notes");
        updated.setLowStockThreshold(5);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Inventory> result = inventoryService.updateInventory(1L, updated);

        assertTrue(result.isPresent());
        assertEquals("Updated Gloves", item.getItemName());
        assertEquals(10, item.getQuantity());
        assertTrue(item.getTotalSpent().compareTo(BigDecimal.valueOf(20.00)) == 0);
        assertTrue(item.getUnitCost().compareTo(BigDecimal.valueOf(3.00)) == 0);
        assertEquals("Updated notes", item.getNotes());
        assertEquals(5, item.getLowStockThreshold());

        verify(financialLogRepository, never()).save(any());
    }

    @Test
    void updateInventory_shouldHandleNullUnitCostGracefully() {
        Inventory updated = new Inventory();
        updated.setItemName("Gloves");
        updated.setQuantity(15); // increased
        updated.setUnitCost(null); // null cost should skip logging

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Inventory> result = inventoryService.updateInventory(1L, updated);

        assertTrue(result.isPresent());
        verify(financialLogRepository, never()).save(any());
    }

    @Test
    void updateInventory_shouldReturnEmpty_whenItemNotFound() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Inventory> result = inventoryService.updateInventory(99L, new Inventory());

        assertTrue(result.isEmpty());
    }

    @Test
    void updateInventory_shouldRejectNegativeUnitCost() {
        Inventory update = new Inventory();
        update.setItemName("Updated Item");
        update.setQuantity(5);
        update.setUnitCost(BigDecimal.valueOf(-5.00)); // Invalid

        Optional<Inventory> result = inventoryService.updateInventory(1L, update);

        assertTrue(result.isEmpty()); // Should not update
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void updateInventory_shouldFail_whenInvalidData() {
        Inventory badUpdate = new Inventory();
        badUpdate.setItemName(null);
        badUpdate.setUnitCost(BigDecimal.valueOf(-2.00));

        Optional<Inventory> result = inventoryService.updateInventory(1L, badUpdate);
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
        assertTrue(item.getTotalSpent().compareTo(BigDecimal.valueOf(30.00)) == 0);

        verify(financialLogRepository).save(argThat(log ->
                log.getType() == FinancialLog.Type.expense
                        && log.getAmount().compareTo(BigDecimal.valueOf(10.00)) == 0
                        && log.getDescription().contains("Added 5 units")
        ));
    }

    @Test
    void addQuantityAndCost_shouldFail_whenInvalid() {
        boolean result = inventoryService.addQuantityAndCost(1L, 0, null);
        assertFalse(result);
    }

    @Test
    void addQuantityAndCost_shouldFail_whenUnitCostIsZero() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        boolean result = inventoryService.addQuantityAndCost(1L, 5, BigDecimal.ZERO);

        assertFalse(result);
        verify(inventoryRepository, never()).save(any());
        verify(financialLogRepository, never()).save(any());
    }

    @Test
    void addQuantityAndCost_shouldFail_whenItemNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = inventoryService.addQuantityAndCost(999L, 5, BigDecimal.valueOf(2.00));

        assertFalse(result);
    }

    @Test
    void removeQuantity_shouldSucceed_whenValid() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean result = inventoryService.removeQuantity(1L, 3);

        assertTrue(result);
        assertEquals(7, item.getQuantity());
        assertTrue(item.getTotalSpent().compareTo(BigDecimal.valueOf(14.00)) == 0);

        verify(financialLogRepository).save(argThat(log ->
                log.getType() == FinancialLog.Type.expense
                        && log.getAmount().compareTo(BigDecimal.valueOf(6.00)) == 0
                        && log.getDescription().contains("Removed 3 units")
        ));
    }

    @Test
    void removeQuantity_shouldFail_whenQuantityExceedsStock() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

        boolean result = inventoryService.removeQuantity(1L, 20);

        assertFalse(result);
        verify(inventoryRepository, never()).save(any());
        verify(financialLogRepository, never()).save(any());
    }

    @Test
    void removeQuantity_shouldFail_whenQuantityIsInvalid() {
        boolean result = inventoryService.removeQuantity(1L, 0);

        assertFalse(result);
        verify(inventoryRepository, never()).findById(anyLong());
        verify(inventoryRepository, never()).save(any());
        verify(financialLogRepository, never()).save(any());
    }
}