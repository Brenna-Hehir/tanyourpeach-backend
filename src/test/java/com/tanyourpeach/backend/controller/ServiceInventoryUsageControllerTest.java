package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.ServiceInventoryUsage;
import com.tanyourpeach.backend.model.ServiceInventoryUsageKey;
import com.tanyourpeach.backend.service.ServiceInventoryUsageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceInventoryUsageControllerTest {

    @Mock
    private ServiceInventoryUsageService usageService;

    @InjectMocks
    private ServiceInventoryUsageController controller;

    private ServiceInventoryUsage usage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usage = new ServiceInventoryUsage();
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey();
        key.setServiceId(1L);
        key.setItemId(100L);
        usage.setId(key);
        usage.setQuantityUsed(2);
    }

    @Test
    void getAllUsages_shouldReturnList() {
        when(usageService.getAllUsages()).thenReturn(List.of(usage));

        List<ServiceInventoryUsage> result = controller.getAllUsages();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId().getServiceId());
    }

    @Test
    void getByService_shouldReturnFilteredList() {
        when(usageService.getByServiceId(1L)).thenReturn(List.of(usage));

        List<ServiceInventoryUsage> result = controller.getByService(1L);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId().getItemId());
    }

    @Test
    void getByItem_shouldReturnFilteredList() {
        when(usageService.getByItemUsageId(100L)).thenReturn(List.of(usage));

        List<ServiceInventoryUsage> result = controller.getByItem(100L);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getQuantityUsed());
    }

    @Test
    void createUsage_shouldReturnOk_ifValid() {
        when(usageService.createUsage(any())).thenReturn(Optional.of(usage));

        ResponseEntity<ServiceInventoryUsage> response = controller.createUsage(usage);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usage, response.getBody());
    }

    @Test
    void createUsage_shouldReturnBadRequest_ifInvalid() {
        when(usageService.createUsage(any())).thenReturn(Optional.empty());

        ResponseEntity<ServiceInventoryUsage> response = controller.createUsage(usage);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateQuantityUsed_shouldReturnOk_ifFound() {
        when(usageService.updateQuantity(1L, 100L, 5)).thenReturn(Optional.of(usage));

        ResponseEntity<ServiceInventoryUsage> response = controller.updateQuantityUsed(1L, 100L, 5);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateQuantityUsed_shouldReturnNotFound_ifNotFound() {
        when(usageService.updateQuantity(1L, 100L, 5)).thenReturn(Optional.empty());

        ResponseEntity<ServiceInventoryUsage> response = controller.updateQuantityUsed(1L, 100L, 5);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteUsage_shouldReturnNoContent_ifDeleted() {
        when(usageService.deleteUsage(1L, 100L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteUsage(1L, 100L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteUsage_shouldReturnNotFound_ifMissing() {
        when(usageService.deleteUsage(1L, 100L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteUsage(1L, 100L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}