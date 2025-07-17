package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.model.TanService;
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

class TanServiceServiceTest {

    @Mock
    private TanServiceRepository serviceRepository;

    @InjectMocks
    private TanServiceService tanServiceService;

    private TanService testService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testService = new TanService();
        testService.setServiceId(1L);
        testService.setName("Glow Tan");
        testService.setDescription("Full body tan");
        testService.setBasePrice(50.0);
        testService.setDurationMinutes(30);
        testService.setIsActive(true);
    }

    @Test
    void getAllServices_shouldReturnList() {
        when(serviceRepository.findAll()).thenReturn(List.of(testService));
        List<TanService> result = tanServiceService.getAllServices();
        assertEquals(1, result.size());
    }

    @Test
    void getServiceById_shouldReturnService_whenExists() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        Optional<TanService> result = tanServiceService.getServiceById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void getServiceById_shouldReturnEmpty_whenNotFound() {
        when(serviceRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<TanService> result = tanServiceService.getServiceById(2L);
        assertTrue(result.isEmpty());
    }

    @Test
    void createService_shouldSaveAndReturn() {
        when(serviceRepository.save(testService)).thenReturn(testService);
        TanService result = tanServiceService.createService(testService);
        assertNotNull(result);
        assertEquals("Glow Tan", result.getName());
    }

    @Test
    void createService_shouldSucceed_withNullDescription() {
        testService.setDescription(null); // allowed
        when(serviceRepository.save(any())).thenReturn(testService);

        TanService result = tanServiceService.createService(testService);

        assertNotNull(result);
        assertNull(result.getDescription());
        verify(serviceRepository).save(any());
    }

    @Test
    void createService_shouldSucceed_withBlankDescription() {
        testService.setDescription("   "); // allowed
        when(serviceRepository.save(any())).thenReturn(testService);

        TanService result = tanServiceService.createService(testService);

        assertNotNull(result);
        assertEquals("   ", result.getDescription());
        verify(serviceRepository).save(any());
    }

    @Test
    void createService_shouldFail_whenInvalid() {
        TanService invalid = new TanService();
        invalid.setName(" "); // blank
        invalid.setDescription(null); // null
        invalid.setBasePrice(0.0); // invalid
        invalid.setDurationMinutes(-10); // invalid

        TanService result = tanServiceService.createService(invalid);

        assertNull(result); // should be rejected
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void updateService_shouldUpdate_whenExists() {
        TanService updated = new TanService();
        updated.setName("New Glow");
        updated.setDescription("Updated desc");
        updated.setBasePrice(60.0);
        updated.setDurationMinutes(40);
        updated.setIsActive(false);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any())).thenReturn(updated);

        Optional<TanService> result = tanServiceService.updateService(1L, updated);
        assertTrue(result.isPresent());
        assertEquals("New Glow", result.get().getName());
    }

    @Test
    void updateService_shouldSucceed_withNullDescription() {
        TanService updated = new TanService();
        updated.setName("Updated Name");
        updated.setDescription(null); // allowed
        updated.setBasePrice(60.0);
        updated.setDurationMinutes(45);
        updated.setIsActive(true);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<TanService> result = tanServiceService.updateService(1L, updated);

        assertTrue(result.isPresent());
        assertNull(result.get().getDescription());
    }

    @Test
    void updateService_shouldReturnEmpty_whenNotFound() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<TanService> result = tanServiceService.updateService(1L, testService);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateService_shouldFail_whenInvalidInput() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));

        TanService invalid = new TanService();
        invalid.setName("  ");
        invalid.setDescription(null);
        invalid.setBasePrice(0.0);
        invalid.setDurationMinutes(0);
        invalid.setIsActive(true);

        Optional<TanService> result = tanServiceService.updateService(1L, invalid);

        assertTrue(result.isEmpty());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deactivateService_shouldSetInactive_whenExists() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        boolean result = tanServiceService.deactivateService(1L);
        assertTrue(result);
        verify(serviceRepository).save(testService);
        assertFalse(testService.getIsActive());
    }

    @Test
    void deactivateService_shouldFail_whenNotFound() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());
        boolean result = tanServiceService.deactivateService(1L);
        assertFalse(result);
    }

    @Test
    void deleteServicePermanently_shouldDelete_whenExists() {
        when(serviceRepository.existsById(1L)).thenReturn(true);
        boolean result = tanServiceService.deleteServicePermanently(1L);
        assertTrue(result);
        verify(serviceRepository).deleteById(1L);
    }

    @Test
    void deleteServicePermanently_shouldFail_whenNotFound() {
        when(serviceRepository.existsById(1L)).thenReturn(false);
        boolean result = tanServiceService.deleteServicePermanently(1L);
        assertFalse(result);
    }
}