package com.tanyourpeach.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.model.User;
import com.tanyourpeach.backend.repository.UserRepository;
import com.tanyourpeach.backend.service.InventoryService;
import com.tanyourpeach.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    // Helper method to check if the user is an admin
    private boolean isAdmin(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email).orElseThrow();
            return user.getIsAdmin() != null && user.getIsAdmin();
        } catch (Exception e) {
            return false;
        }
    }

    // GET all items (allowed for all)
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    // GET by ID (allowed for all)
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create (admin only)
    @PostMapping
    public ResponseEntity<?> createInventory(@RequestBody Inventory inventory, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(inventoryService.createInventory(inventory));
    }

    // PUT update (admin only)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventory(@PathVariable Long id, @RequestBody Inventory updated, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return inventoryService.updateInventory(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE (admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return inventoryService.deleteInventory(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // PUT deduct quantity (used automatically by appointment logic — allow without admin check)
    @PutMapping("/deduct/{id}")
    public ResponseEntity<Void> deductQuantity(@PathVariable Long id, @RequestParam int amount) {
        return inventoryService.deductQuantity(id, amount)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    // PUT add stock (admin only)
    @PutMapping("/add-stock/{id}")
    public ResponseEntity<?> addStock(@PathVariable Long id,
                                      @RequestParam int quantity,
                                      @RequestParam BigDecimal unitCost,
                                      HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return inventoryService.addQuantityAndCost(id, quantity, unitCost)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }
}