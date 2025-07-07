package com.tanurpeach.backend.repository;

import com.tanurpeach.backend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // findByItemName can help prevent duplicates
    Inventory findByItemName(String itemName);
}
