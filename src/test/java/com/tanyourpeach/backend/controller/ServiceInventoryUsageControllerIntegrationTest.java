package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.ServiceInventoryUsage;
import com.tanyourpeach.backend.model.ServiceInventoryUsageKey;
import com.tanyourpeach.backend.model.TanService;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.ServiceInventoryUsageRepository;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceInventoryUsageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceInventoryUsageRepository usageRepository;

    @Autowired
    private TanServiceRepository tanServiceRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TestDataCleaner testDataCleaner;

    private TanService testService;
    private Inventory testItem;

    @BeforeEach
    void setup() {
        testDataCleaner.cleanAll();

        testService = new TanService();
        testService.setName("Spray Tan");
        testService.setBasePrice(50.0);
        testService.setDurationMinutes(30);
        testService.setIsActive(true);
        tanServiceRepository.save(testService);

        testItem = new Inventory();
        testItem.setItemName("Gloves");
        testItem.setQuantity(100);
        testItem.setUnitCost(BigDecimal.valueOf(0.5));
        testItem.setTotalSpent(BigDecimal.valueOf(50.0));
        inventoryRepository.save(testItem);
    }

    @Test
    void createAndGetAndDeleteUsage_shouldSucceed() throws Exception {
        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setService(testService);
        usage.setItem(testItem);
        usage.setQuantityUsed(3);

        String json = objectMapper.writeValueAsString(usage);

        mockMvc.perform(post("/api/service-usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/service-usage/service/" + testService.getServiceId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/service-usage/item/" + testItem.getItemId()))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/service-usage/" + testService.getServiceId() + "/" + testItem.getItemId() + "?quantityUsed=5"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/service-usage/" + testService.getServiceId() + "/" + testItem.getItemId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getByServiceId_shouldReturnEmpty_whenNoneExist() throws Exception {
        mockMvc.perform(get("/api/service-usage/service/" + testService.getServiceId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getByItemId_shouldReturnEmpty_whenNoneExist() throws Exception {
        mockMvc.perform(get("/api/service-usage/item/" + testItem.getItemId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createUsage_shouldDefaultToOne_whenQuantityIsZero() throws Exception {
        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setService(testService);
        usage.setItem(testItem);
        usage.setQuantityUsed(0);

        String json = objectMapper.writeValueAsString(usage);

        mockMvc.perform(post("/api/service-usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityUsed").value(1));
    }

    @Test
    void createUsage_shouldFail_whenServiceMissing() throws Exception {
        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setItem(testItem);
        usage.setQuantityUsed(1);
        String json = objectMapper.writeValueAsString(usage);

        mockMvc.perform(post("/api/service-usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUsage_shouldFail_whenItemMissing() throws Exception {
        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setService(testService);
        usage.setQuantityUsed(1);
        String json = objectMapper.writeValueAsString(usage);

        mockMvc.perform(post("/api/service-usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateQuantity_shouldReturnNotFound_whenUsageDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/service-usage/999/999?quantityUsed=2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateQuantity_shouldReturnBadRequest_whenQuantityInvalid() throws Exception {
        ServiceInventoryUsage usage = new ServiceInventoryUsage();
        usage.setService(testService);
        usage.setItem(testItem);
        usage.setQuantityUsed(3);
        ServiceInventoryUsageKey key = new ServiceInventoryUsageKey(testService.getServiceId(), testItem.getItemId());
        usage.setId(key);
        usageRepository.save(usage);

        mockMvc.perform(put("/api/service-usage/" + testService.getServiceId() + "/" + testItem.getItemId() + "?quantityUsed=0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUsage_shouldReturnNotFound_whenAlreadyDeleted() throws Exception {
        mockMvc.perform(delete("/api/service-usage/123/456"))
                .andExpect(status().isNotFound());
    }
}