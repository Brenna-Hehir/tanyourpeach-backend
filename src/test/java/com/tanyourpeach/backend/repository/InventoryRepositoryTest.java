package com.tanyourpeach.backend.repository;

import com.tanyourpeach.backend.model.Inventory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    // clean H2 in-memory DB in MySQL mode so "ON UPDATE CURRENT_TIMESTAMP" works
    "spring.datasource.url=jdbc:h2:mem:invrepo;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",

    // let Hibernate create/drop tables from your entities
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",

    // keep the slice small / avoid external infra
    "spring.sql.init.mode=never",
    "spring.flyway.enabled=false",
    "spring.liquibase.enabled=false",
    "spring.cloud.gcp.secretmanager.enabled=false",
    "spring.cloud.gcp.sql.enabled=false"
})
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    private Inventory inv(String name, int qty, int threshold, String unitCost, String totalSpent) {
        Inventory i = new Inventory();
        i.setItemName(name);
        i.setQuantity(qty);
        i.setLowStockThreshold(threshold);
        i.setUnitCost(new BigDecimal(unitCost));
        i.setTotalSpent(new BigDecimal(totalSpent));
        i.setNotes(null);
        return i;
    }

    @Test
    @DisplayName("findItemsBelowThreshold: returns items where quantity <= per-row lowStockThreshold")
    void findItemsBelowThreshold_basic() {
        // below/equal/above thresholds
        Inventory gloves   = inventoryRepository.save(inv("Gloves",   0, 5,  "0.10", "0.00")); // 0 <= 5 → include
        Inventory caps     = inventoryRepository.save(inv("Caps",     3, 5,  "0.20", "0.00")); // 3 <= 5 → include
        Inventory solution = inventoryRepository.save(inv("Solution",10, 5,  "20.00","100.00")); // 10 > 5 → exclude

        // null threshold should NOT match (comparison becomes unknown)
        Inventory towels   = inventoryRepository.save(inv("Towels",   2, 0,  "0.50", "4.50"));
        towels.setLowStockThreshold(null);
        inventoryRepository.save(towels);

        List<Inventory> results = inventoryRepository.findItemsBelowThreshold();

        assertEquals(2, results.size(), "Should include only rows with qty <= threshold, skip null thresholds");
        assertTrue(results.stream().anyMatch(i -> i.getItemName().equals("Gloves")   && i.getQuantity() == 0));
        assertTrue(results.stream().anyMatch(i -> i.getItemName().equals("Caps")     && i.getQuantity() == 3));
        assertTrue(results.stream().noneMatch(i -> i.getItemName().equals("Solution")));
        assertTrue(results.stream().noneMatch(i -> i.getItemName().equals("Towels")));
    }

    @Test
    @DisplayName("findItemsBelowThreshold: empty when all quantities above their thresholds")
    void findItemsBelowThreshold_noneMatch() {
        inventoryRepository.save(inv("Bottles", 12, 5, "1.00", "12.00"));
        inventoryRepository.save(inv("Wipes",    9, 5, "0.30", "2.70"));

        List<Inventory> results = inventoryRepository.findItemsBelowThreshold();
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findByItemName: returns exact match")
    void findByItemName_exactMatch() {
        inventoryRepository.save(inv("Caps", 3, 5, "0.20", "0.00"));

        Inventory found = inventoryRepository.findByItemName("Caps");
        assertNotNull(found);
        assertEquals("Caps", found.getItemName());
        assertEquals(3, found.getQuantity());
    }

    @Test
    @DisplayName("findByItemName: returns null for unknown name")
    void findByItemName_unknown() {
        assertNull(inventoryRepository.findByItemName("NotARealItem"));
    }
}