package com.tanyourpeach.backend.controller;

import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.InventoryService;
import com.tanyourpeach.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private InventoryController controller;

    private Inventory testItem;
    private User adminUser;
    private final String token = "Bearer mock-token";
    private final String email = "admin@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testItem = new Inventory();
        testItem.setItemId(1L);

        adminUser = new User();
        adminUser.setEmail(email);
        adminUser.setIsAdmin(true);

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));
    }

    @Test
    void getAllInventory_shouldReturnList() {
        when(inventoryService.getAllInventory()).thenReturn(List.of(testItem));

        List<Inventory> result = controller.getAllInventory();
        assertEquals(1, result.size());
    }

    @Test
    void getInventoryById_shouldReturnItem() {
        when(inventoryService.getInventoryById(1L)).thenReturn(Optional.of(testItem));

        ResponseEntity<Inventory> response = controller.getInventoryById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getInventoryById_shouldReturn404_ifNotFound() {
        when(inventoryService.getInventoryById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Inventory> response = controller.getInventoryById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createInventory_shouldReturnItem_ifAdmin() {
        when(inventoryService.createInventory(testItem)).thenReturn(testItem);

        ResponseEntity<?> response = controller.createInventory(testItem, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createInventory_shouldReturn403_ifNotAdmin() {
        adminUser.setIsAdmin(false);
        ResponseEntity<?> response = controller.createInventory(testItem, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createInventory_shouldReturn403_whenAuthorizationHeaderMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);
        ResponseEntity<?> response = controller.createInventory(testItem, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createInventory_shouldReturn403_whenAuthorizationHeaderMalformed() {
        when(request.getHeader("Authorization")).thenReturn("bad");
        ResponseEntity<?> response = controller.createInventory(testItem, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateInventory_shouldReturnItem_ifFound() {
        when(inventoryService.updateInventory(eq(1L), any())).thenReturn(Optional.of(testItem));

        ResponseEntity<?> response = controller.updateInventory(1L, testItem, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateInventory_shouldReturn403_whenUserNotFound() {
        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("mock-token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateInventory(1L, testItem, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateInventory_shouldReturn403_ifNotAdmin() {
        adminUser.setIsAdmin(false);
        ResponseEntity<?> response = controller.updateInventory(1L, testItem, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateInventory_shouldReturn404_ifNotFound() {
        when(inventoryService.updateInventory(eq(1L), any())).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateInventory(1L, testItem, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteInventory_shouldReturn204_ifDeleted() {
        when(inventoryService.deleteInventory(1L)).thenReturn(true);

        ResponseEntity<?> response = controller.deleteInventory(1L, request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteInventory_shouldReturn403_ifNotAdmin() {
        adminUser.setIsAdmin(false);
        ResponseEntity<?> response = controller.deleteInventory(1L, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteInventory_shouldReturn404_ifNotFound() {
        when(inventoryService.deleteInventory(1L)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteInventory(1L, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deductQuantity_shouldReturn200_ifSuccessful() {
        when(inventoryService.deductQuantity(1L, 2)).thenReturn(true);

        ResponseEntity<Void> response = controller.deductQuantity(1L, 2);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deductQuantity_shouldReturn400_ifFails() {
        when(inventoryService.deductQuantity(1L, 2)).thenReturn(false);

        ResponseEntity<Void> response = controller.deductQuantity(1L, 2);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void addStock_shouldReturn200_ifSuccessAndAdmin() {
        when(inventoryService.addQuantityAndCost(1L, 5, new BigDecimal("2.50"))).thenReturn(true);

        ResponseEntity<?> response = controller.addStock(1L, 5, new BigDecimal("2.50"), request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addStock_shouldReturn400_ifFails() {
        when(inventoryService.addQuantityAndCost(1L, 5, new BigDecimal("2.50"))).thenReturn(false);

        ResponseEntity<?> response = controller.addStock(1L, 5, new BigDecimal("2.50"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void addStock_shouldReturn403_ifNotAdmin() {
        adminUser.setIsAdmin(false);
        ResponseEntity<?> response = controller.addStock(1L, 5, new BigDecimal("2.50"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}