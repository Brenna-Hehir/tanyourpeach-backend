package com.tanyourpeach.backend.service;

import com.tanyourpeach.backend.dto.ServiceCreateRequest;
import com.tanyourpeach.backend.dto.ServiceResponseDto;
import com.tanyourpeach.backend.dto.ServiceUpdateRequest;
import com.tanyourpeach.backend.model.ServiceType;
import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.repository.TanServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

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

    private TanService mainService;
    private TanService addOn;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mainService = new TanService();
        mainService.setServiceId(1L);
        mainService.setName("Peach Cobbler");
        mainService.setSlug("peach-cobbler");
        mainService.setShortDescription("Rapid custom glow.");
        mainService.setDescription("Full body rapid spray tan.");
        mainService.setBasePrice(60.0);
        mainService.setDurationMinutes(30);
        mainService.setServiceType(ServiceType.MAIN_SERVICE);
        mainService.setDisplayOrder(1);
        mainService.setRinseTimeMinHours(2.0);
        mainService.setRinseTimeMaxHours(4.0);
        mainService.setIsActive(true);

        addOn = new TanService();
        addOn.setServiceId(2L);
        addOn.setName("pH Spray");
        addOn.setDescription("Prep spray add-on.");
        addOn.setBasePrice(10.0);
        addOn.setDurationMinutes(0);
        addOn.setServiceType(ServiceType.ADD_ON);
        addOn.setDisplayOrder(1);
        addOn.setIsActive(true);
    }

    @Test
    void getActiveMainServices_shouldReturnOnlyMainServices() {
        when(serviceRepository.findByIsActiveTrueAndServiceTypeOrderByDisplayOrderAscNameAsc(ServiceType.MAIN_SERVICE))
                .thenReturn(List.of(mainService));

        List<ServiceResponseDto> result = tanServiceService.getActiveMainServices();

        assertEquals(1, result.size());
        assertEquals("Peach Cobbler", result.get(0).getName());
        assertEquals(ServiceType.MAIN_SERVICE, result.get(0).getServiceType());
    }

    @Test
    void getActiveAddOns_shouldReturnOnlyAddOns() {
        when(serviceRepository.findByIsActiveTrueAndServiceTypeOrderByDisplayOrderAscNameAsc(ServiceType.ADD_ON))
                .thenReturn(List.of(addOn));

        List<ServiceResponseDto> result = tanServiceService.getActiveAddOns();

        assertEquals(1, result.size());
        assertEquals("pH Spray", result.get(0).getName());
        assertEquals(ServiceType.ADD_ON, result.get(0).getServiceType());
    }

    @Test
    void getAllServicesForAdmin_shouldReturnAllServices() {
        when(serviceRepository.findAllByOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(mainService, addOn));

        List<ServiceResponseDto> result = tanServiceService.getAllServicesForAdmin();

        assertEquals(2, result.size());
    }

    @Test
    void getActiveServiceById_shouldReturnService_whenExistsAndActive() {
        when(serviceRepository.findByServiceIdAndIsActiveTrue(1L)).thenReturn(Optional.of(mainService));

        Optional<ServiceResponseDto> result = tanServiceService.getActiveServiceById(1L);

        assertTrue(result.isPresent());
        assertEquals("Peach Cobbler", result.get().getName());
    }

    @Test
    void getActiveServiceById_shouldReturnEmpty_whenNotFoundOrInactive() {
        when(serviceRepository.findByServiceIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

        Optional<ServiceResponseDto> result = tanServiceService.getActiveServiceById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getActiveServiceBySlug_shouldReturnService_whenExistsAndActive() {
        when(serviceRepository.findBySlugAndIsActiveTrue("peach-cobbler")).thenReturn(Optional.of(mainService));

        Optional<ServiceResponseDto> result = tanServiceService.getActiveServiceBySlug("peach-cobbler");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getServiceId());
    }

    @Test
    void getActiveServiceBySlug_shouldReturnEmpty_whenNotFoundOrInactive() {
        when(serviceRepository.findBySlugAndIsActiveTrue("missing")).thenReturn(Optional.empty());

        Optional<ServiceResponseDto> result = tanServiceService.getActiveServiceBySlug("missing");

        assertTrue(result.isEmpty());
    }

    @Test
    void createService_shouldSaveAndReturnDto() {
        ServiceCreateRequest request = validCreateRequest();

        when(serviceRepository.existsBySlug("peach-cobbler")).thenReturn(false);
        when(serviceRepository.save(any(TanService.class))).thenAnswer(invocation -> {
            TanService saved = invocation.getArgument(0);
            saved.setServiceId(1L);
            return saved;
        });

        ServiceResponseDto result = tanServiceService.createService(request);

        assertNotNull(result);
        assertEquals("Peach Cobbler", result.getName());
        assertEquals("peach-cobbler", result.getSlug());
        assertEquals(ServiceType.MAIN_SERVICE, result.getServiceType());
        assertEquals(1, result.getDisplayOrder());
        verify(serviceRepository).save(any(TanService.class));
    }

    @Test
    void createService_shouldDefaultNullableBooleansAndDisplayFields() {
        ServiceCreateRequest request = validCreateRequest();
        request.setSlug(null);
        request.setIsActive(null);
        request.setDisplayOrder(null);
        request.setServiceType(null);

        when(serviceRepository.save(any(TanService.class))).thenAnswer(invocation -> {
            TanService saved = invocation.getArgument(0);
            saved.setServiceId(1L);
            return saved;
        });

        ServiceResponseDto result = tanServiceService.createService(request);

        assertNull(result.getSlug());
        assertTrue(result.getIsActive());
        assertEquals(0, result.getDisplayOrder());
        assertEquals(ServiceType.MAIN_SERVICE, result.getServiceType());
    }

    @Test
    void createService_shouldThrowConflict_whenSlugAlreadyExists() {
        ServiceCreateRequest request = validCreateRequest();

        when(serviceRepository.existsBySlug("peach-cobbler")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tanServiceService.createService(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Service slug already exists", ex.getReason());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void updateService_shouldUpdateAllEditableFields_whenExists() {
        ServiceUpdateRequest request = validUpdateRequest();
        request.setName("Blackberry Cobbler");
        request.setSlug("blackberry-cobbler");
        request.setBasePrice(70.0);
        request.setDurationMinutes(45);
        request.setIsActive(false);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(mainService));
        when(serviceRepository.existsBySlugAndServiceIdNot("blackberry-cobbler", 1L)).thenReturn(false);
        when(serviceRepository.save(any(TanService.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ServiceResponseDto> result = tanServiceService.updateService(1L, request);

        assertTrue(result.isPresent());
        assertEquals("Blackberry Cobbler", result.get().getName());
        assertEquals("blackberry-cobbler", result.get().getSlug());
        assertEquals(70.0, result.get().getBasePrice());
        assertEquals(45, result.get().getDurationMinutes());
        assertFalse(result.get().getIsActive());
    }

    @Test
    void updateService_shouldReturnEmpty_whenNotFound() {
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ServiceResponseDto> result = tanServiceService.updateService(99L, validUpdateRequest());

        assertTrue(result.isEmpty());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void updateService_shouldThrowConflict_whenSlugBelongsToAnotherService() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(mainService));
        when(serviceRepository.existsBySlugAndServiceIdNot("peach-cobbler", 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tanServiceService.updateService(1L, validUpdateRequest())
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Service slug already exists", ex.getReason());
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void deactivateService_shouldSetInactive_whenExists() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(mainService));

        boolean result = tanServiceService.deactivateService(1L);

        assertTrue(result);
        assertFalse(mainService.getIsActive());
        verify(serviceRepository).save(mainService);
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

    private ServiceCreateRequest validCreateRequest() {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setName("Peach Cobbler");
        request.setSlug("peach-cobbler");
        request.setShortDescription("Rapid custom glow.");
        request.setDescription("Full body rapid spray tan.");
        request.setBasePrice(60.0);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);
        request.setCardImageUrl("https://example.com/card.jpg");
        request.setHeroImageUrl("https://example.com/hero.jpg");
        request.setDisplayOrder(1);
        request.setRinseTimeMinHours(2.0);
        request.setRinseTimeMaxHours(4.0);
        request.setIsActive(true);
        return request;
    }

    private ServiceUpdateRequest validUpdateRequest() {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setName("Peach Cobbler");
        request.setSlug("peach-cobbler");
        request.setShortDescription("Rapid custom glow.");
        request.setDescription("Full body rapid spray tan.");
        request.setBasePrice(60.0);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);
        request.setCardImageUrl("https://example.com/card.jpg");
        request.setHeroImageUrl("https://example.com/hero.jpg");
        request.setDisplayOrder(1);
        request.setRinseTimeMinHours(2.0);
        request.setRinseTimeMaxHours(4.0);
        request.setIsActive(true);
        return request;
    }
}
