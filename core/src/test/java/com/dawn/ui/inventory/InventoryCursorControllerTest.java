package com.dawn.ui.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryCursorControllerTest {
    private PlayerInventory inventory;
    private InventoryCursorController controller;

    @BeforeEach
    void setUp() {
        inventory = new PlayerInventory();
        for (int i = 0; i < com.dawn.inventory.InventoryConstants.SIZE; i++) {
            inventory.setSlotAtIndex(i, ItemStack.empty());
        }
        Entity entity = new Entity(EntityId.PLAYER, 10f, 10f);
        controller =
                new InventoryCursorController(
                        inventory, new EquipmentInventory(), new DropSystem(), entity, () -> {});
    }

    @Test
    void lmbPickup_emptiesSlotAndFillsCursor() {
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.CRATE, 16));
        controller.onSlotClick(InventorySlotRef.grid(0), true);

        assertTrue(inventory.getSlotAtIndex(0).isEmpty());
        assertEquals(ItemId.CRATE, controller.cursorStack().itemId);
        assertEquals(16, controller.cursorStack().count);
    }

    @Test
    void lmbPlace_onEmptySlotClearsCursor() {
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.CRATE, 8));
        inventory.setSlotAtIndex(1, ItemStack.empty());
        controller.onSlotClick(InventorySlotRef.grid(0), true);
        controller.onSlotClick(InventorySlotRef.grid(1), true);

        assertFalse(controller.hasCursor());
        assertEquals(8, inventory.getSlotAtIndex(1).count);
        assertTrue(inventory.getSlotAtIndex(0).isEmpty());
    }

    @Test
    void lmbMerge_stacksSameItem() {
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.SAND, 3));
        inventory.setSlotAtIndex(1, ItemStack.of(ItemId.SAND, 1));
        controller.onSlotClick(InventorySlotRef.grid(0), true);
        controller.onSlotClick(InventorySlotRef.grid(1), true);

        assertFalse(controller.hasCursor());
        assertEquals(4, inventory.getSlotAtIndex(1).count);
        assertTrue(inventory.getSlotAtIndex(0).isEmpty());
    }

    @Test
    void rmbPickup_takesHalfStack() {
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.CRATE, 9));
        controller.onSlotClick(InventorySlotRef.grid(0), false);

        assertEquals(5, controller.cursorStack().count);
        assertEquals(4, inventory.getSlotAtIndex(0).count);
    }

    @Test
    void rmbPlace_putsOneItem() {
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.CRATE, 5));
        inventory.setSlotAtIndex(1, ItemStack.empty());
        controller.onSlotClick(InventorySlotRef.grid(0), true);
        controller.onSlotClick(InventorySlotRef.grid(1), false);

        assertEquals(4, controller.cursorStack().count);
        assertEquals(1, inventory.getSlotAtIndex(1).count);
    }

    @Test
    void returnCursor_restoresToOriginSlot() {
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.BED, 4));
        controller.onSlotClick(InventorySlotRef.grid(0), true);
        controller.returnCursorToInventory();

        assertFalse(controller.hasCursor());
        assertEquals(4, inventory.getSlotAtIndex(0).count);
    }

    @Test
    void returnCursor_fallsBackToEmptySlotWhenOriginBlocked() {
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.CRATE, 4));
        inventory.setSlotAtIndex(1, ItemStack.of(ItemId.BED, 1));
        controller.onSlotClick(InventorySlotRef.grid(0), true);
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.BED, 1));
        controller.returnCursorToInventory();

        assertFalse(controller.hasCursor());
        assertEquals(4, inventory.getSlotAtIndex(2).count);
    }
}
