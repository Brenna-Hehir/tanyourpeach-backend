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
    void createUsage_shouldFail_whenServiceOrItemMissing() {
        when(tanServiceRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<ServiceInventoryUsage> result = service.createUsage(usage);
        assertTrue(result.isEmpty());
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
    void updateQuantity_shouldFail_whenUsageMissing() {
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(1L, 100L);
        when(usageRepository.findById(key)).thenReturn(Optional.empty());

        Optional<ServiceInventoryUsage> result = service.updateQuantity(1L, 100L, 5);
        assertTrue(result.isEmpty());
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