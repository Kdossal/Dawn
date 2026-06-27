package com.dawn.world.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrateStorageRegistryTest {
    private CrateStorageRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CrateStorageRegistry();
    }

    @Test
    void createAt_getAt_hasAt() {
        CrateStorage created = registry.createAt(3, 7);
        assertNotNull(created);
        assertSame(created, registry.getAt(3, 7));
        assertTrue(registry.hasAt(3, 7));
    }

    @Test
    void removeAt_clearsEntry() {
        registry.createAt(1, 2);
        CrateStorage removed = registry.removeAt(1, 2);
        assertNotNull(removed);
        assertNull(registry.getAt(1, 2));
        assertFalse(registry.hasAt(1, 2));
    }

    @Test
    void getAt_unknownCell_returnsNull() {
        assertNull(registry.getAt(99, 99));
    }
}
