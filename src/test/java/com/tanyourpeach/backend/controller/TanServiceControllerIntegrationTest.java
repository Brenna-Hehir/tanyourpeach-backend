package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.dto.ServiceCreateRequest;
import com.tanyourpeach.backend.dto.ServiceUpdateRequest;
import com.tanyourpeach.backend.model.ServiceType;
import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.repository.TanServiceRepository;
import com.tanyourpeach.backend.util.TestDataCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TanServiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TanServiceRepository tanServiceRepository;

    @Autowired
    private TestDataCleaner testDataCleaner;

    @Autowired
    private ObjectMapper objectMapper;

    private TanService mainService;
    private TanService inactiveMainService;
    private TanService addOn;

    @BeforeEach
    void setup() {
        testDataCleaner.cleanAll();

        mainService = new TanService();
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
        mainService = tanServiceRepository.save(mainService);

        inactiveMainService = new TanService();
        inactiveMainService.setName("Hidden Tan");
        inactiveMainService.setSlug("hidden-tan");
        inactiveMainService.setBasePrice(50.0);
        inactiveMainService.setDurationMinutes(30);
        inactiveMainService.setServiceType(ServiceType.MAIN_SERVICE);
        inactiveMainService.setDisplayOrder(2);
        inactiveMainService.setIsActive(false);
        inactiveMainService = tanServiceRepository.save(inactiveMainService);

        addOn = new TanService();
        addOn.setName("pH Spray");
        addOn.setDescription("Prep spray add-on.");
        addOn.setBasePrice(10.0);
        addOn.setDurationMinutes(0);
        addOn.setServiceType(ServiceType.ADD_ON);
        addOn.setDisplayOrder(1);
        addOn.setIsActive(true);
        addOn = tanServiceRepository.save(addOn);
    }

    @Test
    void getActiveMainServices_shouldReturnOnlyActiveMainServices() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Peach Cobbler"))
                .andExpect(jsonPath("$[0].serviceType").value("MAIN_SERVICE"));
    }

    @Test
    void getActiveAddOns_shouldReturnOnlyActiveAddOns() throws Exception {
        mockMvc.perform(get("/api/services/add-ons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("pH Spray"))
                .andExpect(jsonPath("$[0].serviceType").value("ADD_ON"));
    }

    @Test
    void getAllServicesForAdmin_shouldReturnAllServices() throws Exception {
        mockMvc.perform(get("/api/services/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getServiceById_shouldReturnActiveService() throws Exception {
        mockMvc.perform(get("/api/services/" + mainService.getServiceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Peach Cobbler"))
                .andExpect(jsonPath("$.slug").value("peach-cobbler"))
                .andExpect(jsonPath("$.shortDescription").value("Rapid custom glow."))
                .andExpect(jsonPath("$.basePrice").value(60.0))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.serviceType").value("MAIN_SERVICE"))
                .andExpect(jsonPath("$.displayOrder").value(1))
                .andExpect(jsonPath("$.rinseTimeMinHours").value(2.0))
                .andExpect(jsonPath("$.rinseTimeMaxHours").value(4.0))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getServiceById_shouldReturnNotFound_whenInactive() throws Exception {
        mockMvc.perform(get("/api/services/" + inactiveMainService.getServiceId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getServiceById_shouldReturnNotFound_whenMissing() throws Exception {
        mockMvc.perform(get("/api/services/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getServiceBySlug_shouldReturnActiveService() throws Exception {
        mockMvc.perform(get("/api/services/slug/peach-cobbler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceId").value(mainService.getServiceId()))
                .andExpect(jsonPath("$.name").value("Peach Cobbler"));
    }

    @Test
    void getServiceBySlug_shouldReturnNotFound_whenMissing() throws Exception {
        mockMvc.perform(get("/api/services/slug/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createService_shouldPersistAndReturnCreated() throws Exception {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setName("Blackberry Cobbler");
        request.setSlug("blackberry-cobbler");
        request.setShortDescription("Overnight deep glow.");
        request.setDescription("Full body overnight spray tan.");
        request.setBasePrice(70.0);
        request.setDurationMinutes(45);
        request.setServiceType(ServiceType.MAIN_SERVICE);
        request.setDisplayOrder(2);
        request.setRinseTimeMinHours(8.0);
        request.setRinseTimeMaxHours(12.0);
        request.setIsActive(true);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Blackberry Cobbler"))
                .andExpect(jsonPath("$.slug").value("blackberry-cobbler"))
                .andExpect(jsonPath("$.serviceType").value("MAIN_SERVICE"));

        Optional<TanService> saved = tanServiceRepository.findAll()
                .stream()
                .filter(s -> s.getName().equals("Blackberry Cobbler"))
                .findFirst();

        assertThat(saved).isPresent();
        assertThat(saved.get().getBasePrice()).isEqualTo(70.0);
        assertThat(saved.get().getRinseTimeMinHours()).isEqualTo(8.0);
    }

    @Test
    void createService_shouldDefaultOptionalFields() throws Exception {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setName("Golden Glow");
        request.setBasePrice(65.0);
        request.setDurationMinutes(25);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.displayOrder").value(0))
                .andExpect(jsonPath("$.serviceType").value("MAIN_SERVICE"));
    }

    @Test
    void createAddOn_shouldAllowZeroDuration() throws Exception {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setName("Hydrating Spray");
        request.setBasePrice(0.0);
        request.setDurationMinutes(0);
        request.setServiceType(ServiceType.ADD_ON);
        request.setDisplayOrder(2);
        request.setIsActive(true);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hydrating Spray"))
                .andExpect(jsonPath("$.serviceType").value("ADD_ON"))
                .andExpect(jsonPath("$.basePrice").value(0.0))
                .andExpect(jsonPath("$.durationMinutes").value(0));
    }

    @Test
    void createService_shouldFailValidation_whenMissingName() throws Exception {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setBasePrice(50.0);
        request.setDurationMinutes(20);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMainService_shouldFailValidation_whenBasePriceIsZero() throws Exception {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setName("Invalid Main");
        request.setBasePrice(0.0);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createService_shouldReturnConflict_whenSlugAlreadyExists() throws Exception {
        ServiceCreateRequest request = new ServiceCreateRequest();
        request.setName("Duplicate Peach");
        request.setSlug("peach-cobbler");
        request.setBasePrice(70.0);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateService_shouldModifyFields() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setName("Updated Peach");
        request.setSlug("updated-peach");
        request.setShortDescription("Updated short desc.");
        request.setDescription("Updated full desc.");
        request.setBasePrice(99.0);
        request.setDurationMinutes(40);
        request.setServiceType(ServiceType.MAIN_SERVICE);
        request.setDisplayOrder(5);
        request.setRinseTimeMinHours(3.0);
        request.setRinseTimeMaxHours(5.0);
        request.setIsActive(false);

        mockMvc.perform(put("/api/services/" + mainService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Peach"))
                .andExpect(jsonPath("$.slug").value("updated-peach"))
                .andExpect(jsonPath("$.basePrice").value(99.0))
                .andExpect(jsonPath("$.durationMinutes").value(40))
                .andExpect(jsonPath("$.displayOrder").value(5))
                .andExpect(jsonPath("$.isActive").value(false));

        TanService saved = tanServiceRepository.findById(mainService.getServiceId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Updated Peach");
        assertThat(saved.getSlug()).isEqualTo("updated-peach");
        assertThat(saved.getIsActive()).isFalse();
    }

    @Test
    void updateService_shouldReturnNotFound() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setName("Ghost Update");
        request.setBasePrice(75.0);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);

        mockMvc.perform(put("/api/services/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateService_shouldFailValidation_whenNameIsMissing() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setName(null);
        request.setBasePrice(75.0);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);

        mockMvc.perform(put("/api/services/" + mainService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateService_shouldFailValidation_whenBasePriceIsMissing() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setName("Valid Name");
        request.setBasePrice(null);
        request.setDurationMinutes(30);
        request.setServiceType(ServiceType.MAIN_SERVICE);

        mockMvc.perform(put("/api/services/" + mainService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMainService_shouldFailValidation_whenDurationIsZero() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setName("Valid Name");
        request.setBasePrice(50.0);
        request.setDurationMinutes(0);
        request.setServiceType(ServiceType.MAIN_SERVICE);

        mockMvc.perform(put("/api/services/" + mainService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateService_shouldReturnConflict_whenSlugBelongsToAnotherService() throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest();
        request.setName("Slug Conflict");
        request.setSlug("hidden-tan");
        request.setBasePrice(80.0);
        request.setDurationMinutes(40);
        request.setServiceType(ServiceType.MAIN_SERVICE);

        mockMvc.perform(put("/api/services/" + mainService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deactivateService_shouldSetInactive() throws Exception {
        mockMvc.perform(delete("/api/services/" + mainService.getServiceId()))
                .andExpect(status().isNoContent());

        Optional<TanService> updated = tanServiceRepository.findById(mainService.getServiceId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getIsActive()).isFalse();
    }

    @Test
    void deleteServicePermanently_shouldRemove() throws Exception {
        mockMvc.perform(delete("/api/services/" + mainService.getServiceId() + "/force"))
                .andExpect(status().isNoContent());

        assertThat(tanServiceRepository.findById(mainService.getServiceId())).isEmpty();
    }

    @Test
    void deleteService_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/services/999999"))
                .andExpect(status().isNotFound());
    }
}
