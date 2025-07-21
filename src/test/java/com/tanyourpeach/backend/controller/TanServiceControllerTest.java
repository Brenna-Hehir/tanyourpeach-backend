package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.service.TanServiceService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class TanServiceControllerTest {

    @Mock
    private TanServiceService serviceService;

    @InjectMocks
    private TanServiceController controller;

    private TanService testService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testService = new TanService();
        testService.setServiceId(1L);
        testService.setName("Basic Tan");
        testService.setBasePrice(50.0);
    }

    @Test
    void getAllServices_shouldReturnList() {
        when(serviceService.getAllServices()).thenReturn(List.of(testService));
        List<TanService> result = controller.getAllServices();
        assertEquals(1, result.size());
        assertEquals("Basic Tan", result.get(0).getName());
    }

    @Test
    void getServiceById_shouldReturnService_ifExists() {
        when(serviceService.getServiceById(1L)).thenReturn(Optional.of(testService));
        ResponseEntity<TanService> response = controller.getServiceById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testService, response.getBody());
    }

    @Test
    void getServiceById_shouldReturn404_ifNotFound() {
        when(serviceService.getServiceById(1L)).thenReturn(Optional.empty());
        ResponseEntity<TanService> response = controller.getServiceById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createService_shouldReturnCreatedService() {
        when(serviceService.createService(testService)).thenReturn(testService);

        ResponseEntity<TanService> response = controller.createService(testService);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testService, response.getBody());
    }

    @Test
    void createService_shouldReturn400_whenServiceIsNull() {
        when(serviceService.createService(null)).thenReturn(null);
        ResponseEntity<TanService> response = controller.createService(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateService_shouldReturnUpdatedService_ifExists() {
        when(serviceService.updateService(eq(1L), any())).thenReturn(Optional.of(testService));
        ResponseEntity<TanService> response = controller.updateService(1L, testService);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testService, response.getBody());
    }

    @Test
    void updateService_shouldHandleNullUpdateObject() {
        when(serviceService.updateService(eq(1L), isNull())).thenReturn(Optional.empty());
        ResponseEntity<TanService> response = controller.updateService(1L, null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateService_shouldReturn404_ifNotFound() {
        when(serviceService.updateService(eq(1L), any())).thenReturn(Optional.empty());
        ResponseEntity<TanService> response = controller.updateService(1L, testService);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deactivateService_shouldReturn204_ifSuccessful() {
        when(serviceService.deactivateService(1L)).thenReturn(true);
        ResponseEntity<Void> response = controller.deactivateService(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deactivateService_shouldReturn404_ifNotFound() {
        when(serviceService.deactivateService(1L)).thenReturn(false);
        ResponseEntity<Void> response = controller.deactivateService(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteServicePermanently_shouldReturn204_ifSuccessful() {
        when(serviceService.deleteServicePermanently(1L)).thenReturn(true);
        ResponseEntity<Void> response = controller.deleteServicePermanently(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteServicePermanently_shouldReturn404_ifNotFound() {
        when(serviceService.deleteServicePermanently(1L)).thenReturn(false);
        ResponseEntity<Void> response = controller.deleteServicePermanently(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}