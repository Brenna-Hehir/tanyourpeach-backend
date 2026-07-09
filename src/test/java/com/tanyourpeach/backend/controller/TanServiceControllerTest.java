package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.dto.ServiceCreateRequest;
import com.tanyourpeach.backend.dto.ServiceResponseDto;
import com.tanyourpeach.backend.dto.ServiceUpdateRequest;
import com.tanyourpeach.backend.model.ServiceType;
import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.service.TanServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TanServiceControllerTest {

    @Mock
    private TanServiceService serviceService;

    @InjectMocks
    private TanServiceController controller;

    private ServiceResponseDto responseDto;
    private ServiceCreateRequest createRequest;
    private ServiceUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        TanService service = new TanService();
        service.setServiceId(1L);
        service.setName("Peach Cobbler");
        service.setSlug("peach-cobbler");
        service.setBasePrice(60.0);
        service.setDurationMinutes(30);
        service.setServiceType(ServiceType.MAIN_SERVICE);
        service.setDisplayOrder(1);
        service.setIsActive(true);

        responseDto = new ServiceResponseDto(service);

        createRequest = new ServiceCreateRequest();
        createRequest.setName("Peach Cobbler");
        createRequest.setSlug("peach-cobbler");
        createRequest.setBasePrice(60.0);
        createRequest.setDurationMinutes(30);
        createRequest.setServiceType(ServiceType.MAIN_SERVICE);

        updateRequest = new ServiceUpdateRequest();
        updateRequest.setName("Peach Cobbler");
        updateRequest.setSlug("peach-cobbler");
        updateRequest.setBasePrice(60.0);
        updateRequest.setDurationMinutes(30);
        updateRequest.setServiceType(ServiceType.MAIN_SERVICE);
    }

    @Test
    void getActiveMainServices_shouldReturnList() {
        when(serviceService.getActiveMainServices()).thenReturn(List.of(responseDto));

        List<ServiceResponseDto> result = controller.getActiveMainServices();

        assertEquals(1, result.size());
        assertEquals("Peach Cobbler", result.get(0).getName());
    }

    @Test
    void getActiveAddOns_shouldReturnList() {
        when(serviceService.getActiveAddOns()).thenReturn(List.of(responseDto));

        List<ServiceResponseDto> result = controller.getActiveAddOns();

        assertEquals(1, result.size());
    }

    @Test
    void getAllServicesForAdmin_shouldReturnList() {
        when(serviceService.getAllServicesForAdmin()).thenReturn(List.of(responseDto));

        List<ServiceResponseDto> result = controller.getAllServicesForAdmin();

        assertEquals(1, result.size());
    }

    @Test
    void getServiceById_shouldReturnService_ifExists() {
        when(serviceService.getActiveServiceById(1L)).thenReturn(Optional.of(responseDto));

        ResponseEntity<ServiceResponseDto> response = controller.getServiceById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getServiceById_shouldReturn404_ifNotFound() {
        when(serviceService.getActiveServiceById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getServiceById(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Service not found", ex.getReason());
    }

    @Test
    void getServiceBySlug_shouldReturnService_ifExists() {
        when(serviceService.getActiveServiceBySlug("peach-cobbler")).thenReturn(Optional.of(responseDto));

        ResponseEntity<ServiceResponseDto> response = controller.getServiceBySlug("peach-cobbler");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getServiceBySlug_shouldReturn404_ifNotFound() {
        when(serviceService.getActiveServiceBySlug("missing")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getServiceBySlug("missing")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Service not found", ex.getReason());
    }

    @Test
    void createService_shouldReturnCreatedService() {
        when(serviceService.createService(createRequest)).thenReturn(responseDto);

        ResponseEntity<ServiceResponseDto> response = controller.createService(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void updateService_shouldReturnUpdatedService() {
        when(serviceService.updateService(1L, updateRequest)).thenReturn(Optional.of(responseDto));

        ResponseEntity<ServiceResponseDto> response = controller.updateService(1L, updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void updateService_shouldReturn404_ifNotFound() {
        when(serviceService.updateService(1L, updateRequest)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateService(1L, updateRequest)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Service not found", ex.getReason());
    }

    @Test
    void deactivateService_shouldReturnNoContent_whenSuccessful() {
        when(serviceService.deactivateService(1L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deactivateService(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deactivateService_shouldReturn404_whenMissing() {
        when(serviceService.deactivateService(1L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.deactivateService(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Service not found", ex.getReason());
    }

    @Test
    void deleteServicePermanently_shouldReturnNoContent_whenSuccessful() {
        when(serviceService.deleteServicePermanently(1L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteServicePermanently(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteServicePermanently_shouldReturn404_whenMissing() {
        when(serviceService.deleteServicePermanently(1L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.deleteServicePermanently(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Service not found", ex.getReason());
    }
}
