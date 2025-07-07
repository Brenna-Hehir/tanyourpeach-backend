package com.tanyourpeach.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tanyourpeach.backend.model.Inventory;
import com.tanyourpeach.backend.service.InventoryService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // GET all items
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create
    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    // PUT update
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory updated) {
        return inventoryService.updateInventory(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        return inventoryService.deleteInventory(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // PUT deduct quantity
    @PutMapping("/deduct/{id}")
    public ResponseEntity<Void> deductQuantity(@PathVariable Long id, @RequestParam int amount) {
        return inventoryService.deductQuantity(id, amount)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    // PUT add quantity and cost
    @PutMapping("/add-stock/{id}")
    public ResponseEntity<Void> addStock(@PathVariable Long id,
                                         @RequestParam int quantity,
                                         @RequestParam BigDecimal unitCost) {
        return inventoryService.addQuantityAndCost(id, quantity, unitCost)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }
}