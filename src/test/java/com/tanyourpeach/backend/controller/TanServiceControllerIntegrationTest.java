package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.repository.AppointmentRepository;
import com.tanyourpeach.backend.repository.AvailabilityRepository;
import com.tanyourpeach.backend.repository.TanServiceRepository;
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
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TanService testService;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        tanServiceRepository.deleteAll();

        testService = new TanService();
        testService.setName("Spray Tan Deluxe");
        testService.setBasePrice(75.0);
        testService.setDurationMinutes(30);
        testService.setIsActive(true);
        testService = tanServiceRepository.save(testService);
    }

    @Test
    void getAllServices_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Spray Tan Deluxe"));
    }

    @Test
    void getServiceById_shouldReturnService() throws Exception {
        mockMvc.perform(get("/api/services/" + testService.getServiceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spray Tan Deluxe"));
    }

    @Test
    void getServiceById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/services/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createService_shouldPersistAndReturnCreated() throws Exception {
        TanService newService = new TanService();
        newService.setName("Express Glow");
        newService.setBasePrice(60.0);
        newService.setDurationMinutes(20);
        newService.setIsActive(true);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newService)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Express Glow"));

        Optional<TanService> saved = tanServiceRepository.findAll()
                .stream().filter(s -> s.getName().equals("Express Glow")).findFirst();
        assertThat(saved).isPresent();
        assertThat(saved.get().getBasePrice()).isEqualTo(60.0);
    }

    @Test
    void createService_shouldFailValidation_whenMissingName() throws Exception {
        TanService invalid = new TanService();
        invalid.setBasePrice(50.0);
        invalid.setDurationMinutes(20);

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateService_shouldModifyFields() throws Exception {
        testService.setName("Updated Tan");
        testService.setBasePrice(99.0);

        mockMvc.perform(put("/api/services/" + testService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Tan"))
                .andExpect(jsonPath("$.basePrice").value(99.0));
    }

    @Test
    void updateService_shouldReturnNotFound() throws Exception {
        testService.setName("Ghost Update");

        mockMvc.perform(put("/api/services/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testService)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateService_shouldFail_whenNameIsMissing() throws Exception {
        TanService invalid = new TanService();
        invalid.setName(null);
        invalid.setBasePrice(75.0);
        invalid.setDurationMinutes(30);
        invalid.setIsActive(true);

        mockMvc.perform(put("/api/services/" + testService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateService_shouldFail_whenBasePriceIsMissing() throws Exception {
        TanService invalid = new TanService();
        invalid.setName("Valid Name");
        invalid.setBasePrice(null);
        invalid.setDurationMinutes(30);
        invalid.setIsActive(true);

        mockMvc.perform(put("/api/services/" + testService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateService_shouldFail_whenBasePriceIsZero() throws Exception {
        TanService invalid = new TanService();
        invalid.setName("Valid Name");
        invalid.setBasePrice(0.0);
        invalid.setDurationMinutes(30);
        invalid.setIsActive(true);

        mockMvc.perform(put("/api/services/" + testService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateService_shouldFail_whenDurationIsMissing() throws Exception {
        TanService invalid = new TanService();
        invalid.setName("Valid Name");
        invalid.setBasePrice(50.0);
        invalid.setDurationMinutes(null);
        invalid.setIsActive(true);

        mockMvc.perform(put("/api/services/" + testService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateService_shouldFail_whenDurationIsZero() throws Exception {
        TanService invalid = new TanService();
        invalid.setName("Valid Name");
        invalid.setBasePrice(50.0);
        invalid.setDurationMinutes(0);
        invalid.setIsActive(true);

        mockMvc.perform(put("/api/services/" + testService.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateService_shouldSetInactive() throws Exception {
        mockMvc.perform(delete("/api/services/" + testService.getServiceId()))
                .andExpect(status().isNoContent());

        Optional<TanService> updated = tanServiceRepository.findById(testService.getServiceId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getIsActive()).isFalse();
    }

    @Test
    void deleteService_shouldRemove() throws Exception {
        mockMvc.perform(delete("/api/services/" + testService.getServiceId() + "/force"))
                .andExpect(status().isNoContent());

        assertThat(tanServiceRepository.findById(testService.getServiceId())).isEmpty();
    }

    @Test
    void deleteService_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/services/999999"))
                .andExpect(status().isNotFound());
    }
}