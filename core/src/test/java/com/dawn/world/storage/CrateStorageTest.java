package com.dawn.world.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import org.junit.jupiter.api.Test;

class CrateStorageTest {
    @Test
    void drainNonEmptySlots_returnsCopiesOfOccupiedSlots() {
        CrateStorage storage = new CrateStorage();
        storage.setSlotAtIndex(0, ItemStack.of(ItemId.SAND, 2));
        storage.setSlotAtIndex(5, ItemStack.of(ItemId.LUMBER, 1));

        var drained = storage.drainNonEmptySlots();

        assertEquals(2, drained.size());
        assertTrue(drained.stream().anyMatch(s -> s.itemId == ItemId.SAND && s.count == 2));
        assertTrue(drained.stream().anyMatch(s -> s.itemId == ItemId.LUMBER && s.count == 1));
        ItemStack slot0 = storage.getSlotAtIndex(0);
        assertEquals(ItemId.SAND, slot0.itemId);
        assertEquals(2, slot0.count);
    }
}
