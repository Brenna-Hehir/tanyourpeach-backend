package com.tanurpeach.backend.controller;

import com.tanurpeach.backend.model.Inventory;
import com.tanurpeach.backend.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // GET all inventory
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    // GET one item
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create inventory
    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    // PUT update inventory
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory updated) {
        return inventoryService.updateInventory(id, updated)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }


    // DELETE item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        return inventoryService.deleteInventory(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // PUT to deduct quantity
    @PutMapping("/deduct/{id}")
    public ResponseEntity<Void> deductQuantity(@PathVariable Long id, @RequestParam int amount) {
        return inventoryService.deductQuantity(id, amount)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }
}