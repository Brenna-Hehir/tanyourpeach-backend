package com.tanyourpeach.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServiceInventoryUsageKeyTest {

    @Test
    @DisplayName("equals: same ids -> equal")
    void equals_same() {
        ServiceInventoryUsageKey k1 = new ServiceInventoryUsageKey(1L, 2L);
        ServiceInventoryUsageKey k2 = new ServiceInventoryUsageKey(1L, 2L);
        assertEquals(k1, k2);
        assertEquals(k1.hashCode(), k2.hashCode());
    }

    @Test
    @DisplayName("equals: different serviceId or itemId -> not equal")
    void equals_different() {
        ServiceInventoryUsageKey base = new ServiceInventoryUsageKey(1L, 2L);
        assertNotEquals(base, new ServiceInventoryUsageKey(9L, 2L));
        assertNotEquals(base, new ServiceInventoryUsageKey(1L, 9L));
        assertNotEquals(base, new ServiceInventoryUsageKey(9L, 9L));
        assertNotEquals(base, null);
        assertNotEquals(base, new Object());
    }

    @Test
    @DisplayName("hashCode works in hash-based collections")
    void hashcode_mapUsage() {
        Map<ServiceInventoryUsageKey, String> map = new HashMap<>();
        ServiceInventoryUsageKey k = new ServiceInventoryUsageKey(3L, 4L);
        map.put(k, "ok");

        assertEquals("ok", map.get(new ServiceInventoryUsageKey(3L, 4L)));
        assertNull(map.get(new ServiceInventoryUsageKey(3L, 5L)));
    }
}