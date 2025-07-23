package com.tanyourpeach.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.InventoryRepository;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    private String userToken;

    private Inventory testItem;

    private Long testItemId;

    @BeforeEach
    void setup() {
        inventoryRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("pass");
        admin.setIsAdmin(true);

        User user = new User();
        user.setName("User");
        user.setEmail("user@example.com");
        user.setPasswordHash("pass");
        user.setIsAdmin(false);
        
        userRepository.save(admin);
        userRepository.save(user);

        adminToken = "Bearer " + jwtService.generateToken(admin);
        userToken = "Bearer " + jwtService.generateToken(user);

        testItem = new Inventory();
        testItem.setItemName("Gloves");
        testItem.setQuantity(10);
        testItem.setUnitCost(BigDecimal.valueOf(2.00));
        testItem.setTotalSpent(BigDecimal.valueOf(20.00));
        testItem.setLowStockThreshold(5);
        inventoryRepository.save(testItem);
    }

    @Test
    void getAllInventory_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId").value(testItem.getItemId()));
    }

    @Test
    void getInventoryById_shouldReturnItem_ifExists() throws Exception {
        mockMvc.perform(get("/api/inventory/" + testItem.getItemId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(testItem.getItemId()));
    }

    @Test
    void getInventoryById_shouldReturn404_ifNotFound() throws Exception {
        mockMvc.perform(get("/api/inventory/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createInventory_shouldSucceed_withValidData() throws Exception {
        Inventory newItem = new Inventory();
        newItem.setItemName("Cap");
        newItem.setQuantity(5);
        newItem.setUnitCost(BigDecimal.valueOf(1.50));
        newItem.setTotalSpent(BigDecimal.valueOf(7.50));
        newItem.setLowStockThreshold(3);

        mockMvc.perform(post("/api/inventory")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("Cap"));
    }

    @Test
    void createInventory_shouldFail_ifNotAdmin() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName("Unauthorized Item");

        mockMvc.perform(post("/api/inventory")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createInventory_shouldFailWithMissingItemName() throws Exception {
        Inventory invalid = new Inventory(); // missing itemName
        invalid.setQuantity(5);
        invalid.setUnitCost(BigDecimal.valueOf(2.00));

        mockMvc.perform(post("/api/inventory")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInventory_shouldFailWithBlankItemName() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName(" "); // Blank string
        invalid.setQuantity(5);
        invalid.setUnitCost(BigDecimal.valueOf(2.00));

        mockMvc.perform(post("/api/inventory")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInventory_shouldFailWithNegativeQuantity() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName("Negative Quantity Test");
        invalid.setQuantity(-5); // Invalid
        invalid.setUnitCost(BigDecimal.valueOf(2.00));

        mockMvc.perform(post("/api/inventory")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInventory_shouldFailWithNegativeUnitCost() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName("Negative Cost Test");
        invalid.setQuantity(5);
        invalid.setUnitCost(BigDecimal.valueOf(-1.00)); // Invalid

        mockMvc.perform(post("/api/inventory")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInventory_shouldSucceed_withValidData() throws Exception {
        testItem.setQuantity(20);

        mockMvc.perform(put("/api/inventory/" + testItem.getItemId())
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void updateInventory_shouldFail_ifNotAdmin() throws Exception {
        mockMvc.perform(put("/api/inventory/" + testItem.getItemId())
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateInventory_shouldFail_ifNotFound() throws Exception {
        testItem.setItemId(9999L);

        mockMvc.perform(put("/api/inventory/9999")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateInventory_shouldFailWithMissingItemName() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName(null); // Missing
        invalid.setQuantity(10);
        invalid.setUnitCost(BigDecimal.valueOf(2.50));

        mockMvc.perform(put("/api/inventory/" + testItemId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInventory_shouldFailWithBlankItemName() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName(" ");
        invalid.setQuantity(10);
        invalid.setUnitCost(BigDecimal.valueOf(2.50));

        mockMvc.perform(put("/api/inventory/" + testItemId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInventory_shouldFailWithNegativeQuantity() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName("Updated Item");
        invalid.setQuantity(-5);
        invalid.setUnitCost(BigDecimal.valueOf(2.50));

        mockMvc.perform(put("/api/inventory/" + testItemId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInventory_shouldFailWithNegativeUnitCost() throws Exception {
        Inventory invalid = new Inventory();
        invalid.setItemName("Updated Item");
        invalid.setQuantity(10);
        invalid.setUnitCost(BigDecimal.valueOf(-3.00));

        mockMvc.perform(put("/api/inventory/" + testItemId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteInventory_shouldSucceed_ifAdmin() throws Exception {
        mockMvc.perform(delete("/api/inventory/" + testItem.getItemId())
                .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        assertThat(inventoryRepository.findById(testItem.getItemId())).isNotPresent();
    }

    @Test
    void deleteInventory_shouldFail_ifNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/inventory/" + testItem.getItemId())
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteInventory_shouldFail_ifNotFound() throws Exception {
        mockMvc.perform(delete("/api/inventory/9999")
                .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deductQuantity_shouldSucceed_whenValid() throws Exception {
        mockMvc.perform(put("/api/inventory/deduct/" + testItem.getItemId())
                .param("amount", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void deductQuantity_shouldReduceStock() throws Exception {
        Inventory item = new Inventory();
        item.setItemName("Gloves");
        item.setQuantity(10);
        item.setUnitCost(new BigDecimal("1.00"));
        item.setTotalSpent(new BigDecimal("10.00"));
        item = inventoryRepository.save(item);

        mockMvc.perform(put("/api/inventory/deduct/" + item.getItemId())
                .param("amount", "4"))
                .andExpect(status().isOk());

        Inventory updated = inventoryRepository.findById(item.getItemId()).orElseThrow();
        assertEquals(6, updated.getQuantity());
    }

    @Test
    void deductQuantity_shouldFail_whenNotEnoughStock() throws Exception {
        mockMvc.perform(put("/api/inventory/deduct/" + testItem.getItemId())
                .param("amount", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deductQuantity_shouldFail_whenItemNotFound() throws Exception {
        mockMvc.perform(put("/api/inventory/deduct/9999")
                .param("amount", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deductQuantity_shouldFailWhenQuantityTooHigh() throws Exception {
        testItem.setQuantity(2);
        inventoryRepository.save(testItem);

        mockMvc.perform(put("/api/inventory/deduct/" + testItem.getItemId())
                .param("amount", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deductQuantity_shouldFailWhenAmountNegative() throws Exception {
        mockMvc.perform(put("/api/inventory/deduct/" + testItem.getItemId())
                .param("amount", "-3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_shouldUpdateTotalSpent() throws Exception {
        Inventory item = new Inventory();
        item.setItemName("Test Item");
        item.setQuantity(5);
        item.setUnitCost(new BigDecimal("2.00"));
        item.setTotalSpent(new BigDecimal("10.00"));
        item = inventoryRepository.save(item);

        mockMvc.perform(put("/api/inventory/add-stock/" + item.getItemId())
                .param("quantity", "3")
                .param("unitCost", "4.00")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());

        Inventory updated = inventoryRepository.findById(item.getItemId()).orElseThrow();
        assertEquals(new BigDecimal("22.00"), updated.getTotalSpent()); // 10.00 + (3 * 4.00)
    }

    @Test
    void addStock_shouldSucceed_ifAdmin() throws Exception {
        mockMvc.perform(put("/api/inventory/add-stock/" + testItem.getItemId())
                .header("Authorization", adminToken)
                .param("quantity", "5")
                .param("unitCost", "1.00"))
                .andExpect(status().isOk());
    }

    @Test
    void addStock_shouldFail_ifNotAdmin() throws Exception {
        mockMvc.perform(put("/api/inventory/add-stock/" + testItem.getItemId())
                .header("Authorization", userToken)
                .param("quantity", "5")
                .param("unitCost", "1.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addStock_shouldFail_withInvalidParams() throws Exception {
        mockMvc.perform(put("/api/inventory/add-stock/" + testItem.getItemId())
                .header("Authorization", adminToken)
                .param("quantity", "-1")
                .param("unitCost", "0.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_shouldFailWhenItemNotFound() throws Exception {
        mockMvc.perform(put("/api/inventory/add-stock/9999")
                .param("quantity", "5")
                .param("unitCost", "2.50")
                .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_shouldFailWhenQuantityInvalid() throws Exception {
        mockMvc.perform(put("/api/inventory/add-stock/" + testItem.getItemId())
                .param("quantity", "-1")
                .param("unitCost", "2.50")
                .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_shouldFailWhenUnitCostMissing() throws Exception {
        mockMvc.perform(put("/api/inventory/add-stock/" + testItem.getItemId())
                .param("quantity", "3")
                .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStock_shouldFailWhenNotAdmin() throws Exception {
        mockMvc.perform(put("/api/inventory/add-stock/" + testItem.getItemId())
                .param("quantity", "3")
                .param("unitCost", "2.50")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }
}