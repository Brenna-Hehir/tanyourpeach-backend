package com.tanyourpeach.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanyourpeach.backend.model.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // findByItemName can help prevent duplicates
    Inventory findByItemName(String itemName);
}
