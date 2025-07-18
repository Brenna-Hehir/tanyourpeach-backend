package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.ServiceInventoryUsage;
import com.tanyourpeach.backend.model.ServiceInventoryUsageKey;
import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.ServiceInventoryUsageRepository;
import com.tanyourpeach.backend.repository.TanServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceInventoryUsageServiceTest {

    @Mock
    private ServiceInventoryUsageRepository usageRepository;

    @Mock
    private TanServiceRepository tanServiceRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ServiceInventoryUsageService service;

    private TanService tanService;
    private Inventory item;
    private ServiceInventoryUsage usage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tanService = new TanService();
        tanService.setServiceId(1L);

        item = new Inventory();
        item.setItemId(100L);

        usage = new ServiceInventoryUsage();
        usage.setService(tanService);
        usage.setItem(item);
        usage.setQuantityUsed(2);
    }

    @Test
    void getAllUsages_shouldReturnList() {
        when(usageRepository.findAll()).thenReturn(List.of(usage));
        List<ServiceInventoryUsage> result = service.getAllUsages();
        assertEquals(1, result.size());
    }

    @Test
    void getByServiceId_shouldReturnList() {
        when(usageRepository.findByService_ServiceId(1L)).thenReturn(List.of(usage));
        List<ServiceInventoryUsage> result = service.getByServiceId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getByItemUsageId_shouldReturnList() {
        when(usageRepository.findByItem_ItemId(100L)).thenReturn(List.of(usage));
        List<ServiceInventoryUsage> result = service.getByItemUsageId(100L);
        assertEquals(1, result.size());
    }

    @Test
    void createUsage_shouldSucceed_whenServiceAndItemExist() {
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(tanService));
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(item));
        when(usageRepository.save(any())).thenReturn(usage);

        Optional<ServiceInventoryUsage> result = service.createUsage(usage);
        assertTrue(result.isPresent());
        verify(usageRepository).save(any());
    }

    @Test
    void createUsage_shouldSetCompositeKeyCorrectly() {
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(tanService));
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(item));
        when(usageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<ServiceInventoryUsage> result = service.createUsage(usage);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId().getServiceId());
        assertEquals(100L, result.get().getId().getItemId());
    }

    @Test
    void createUsage_shouldDefaultQuantityUsedToOne_ifZero() {
        usage.setQuantityUsed(0); // simulate zero

        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(tanService));
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(item));
        when(usageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<ServiceInventoryUsage> result = service.createUsage(usage);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getQuantityUsed());
    }

    @Test
    void createUsage_shouldDefaultQuantityUsedToOne_ifNull() {
        usage.setQuantityUsed(null);
        usage.setService(tanService);
        usage.setItem(item);

        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(tanService));
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(item));
        when(usageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<ServiceInventoryUsage> result = service.createUsage(usage);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getQuantityUsed());
    }

    @Test
    void createUsage_shouldPreserveQuantityUsed_ifPositive() {
        usage.setQuantityUsed(3); // valid input

        when(tanServiceRepository.findById(1L)).thenReturn(Optional.of(tanService));
        when(inventoryRepository.findById(100L)).thenReturn(Optional.of(item));
        when(usageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<ServiceInventoryUsage> result = service.createUsage(usage);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().getQuantityUsed());
    }

    @Test
    void createUsage_shouldFail_whenServiceOrItemMissing() {
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<ServiceInventoryUsage> result = service.createUsage(usage);
        assertTrue(result.isEmpty());
    }

    @Test
    void createUsage_shouldFail_whenServiceOrItemIsNull() {
        usage.setService(null);  // simulate missing service
        usage.setItem(null);     // simulate missing item

        Optional<ServiceInventoryUsage> result = service.createUsage(usage);

        assertTrue(result.isEmpty());
        verify(usageRepository, never()).save(any());
    }

    @Test
    void updateQuantity_shouldSucceed_whenUsageExists() {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(1L, 100L);
        when(usageRepository.findById(key)).thenReturn(Optional.of(usage));
        when(usageRepository.save(any())).thenReturn(usage);

        Optional<ServiceInventoryUsage> result = service.updateQuantity(1L, 100L, 5);
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getQuantityUsed());
    }

    @Test
    void updateQuantity_shouldAllowQuantityOfOne() {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(1L, 100L);
        when(usageRepository.findById(key)).thenReturn(Optional.of(usage));
        when(usageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<ServiceInventoryUsage> result = service.updateQuantity(1L, 100L, 1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getQuantityUsed());
    }

    @Test
    void updateQuantity_shouldFail_whenUsageMissing() {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(1L, 100L);
        when(usageRepository.findById(key)).thenReturn(Optional.empty());

        Optional<ServiceInventoryUsage> result = service.updateQuantity(1L, 100L, 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateQuantity_shouldFail_whenQuantityIsZeroOrNegative() {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(1L, 100L);
        when(usageRepository.findById(key)).thenReturn(Optional.of(usage));

        Optional<ServiceInventoryUsage> resultZero = service.updateQuantity(1L, 100L, 0);
        Optional<ServiceInventoryUsage> resultNegative = service.updateQuantity(1L, 100L, -3);

        assertTrue(resultZero.isEmpty());
        assertTrue(resultNegative.isEmpty());
        verify(usageRepository, never()).save(any());
    }

    @Test
    void deleteUsage_shouldSucceed_whenExists() {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(1L, 100L);
        when(usageRepository.existsById(key)).thenReturn(true);
        boolean result = service.deleteUsage(1L, 100L);
        assertTrue(result);
        verify(usageRepository).deleteById(key);
    }

    @Test
    void deleteUsage_shouldFail_whenMissing() {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(1L, 100L);
        when(usageRepository.existsById(key)).thenReturn(false);
        boolean result = service.deleteUsage(1L, 100L);
        assertFalse(result);
    }
}