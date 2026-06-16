package com.dawn.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.TestInventories;
import org.junit.jupiter.api.Test;

class InventoryWeightTest {
    @Test
    void smallStack_ofFour_equalsQuarterUnit() {
        PlayerInventory inventory = TestInventories.empty();
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.CANNED_FOOD, 4));

        assertEquals(0.25f, InventoryWeight.totalWeight(inventory, null), 0.001f);
    }

    @Test
    void tools_weighTwoEach() {
        PlayerInventory inventory = TestInventories.empty();
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.PICKAXE));
        inventory.setSlotAtIndex(1, ItemStack.of(ItemId.AXE));
        inventory.setSlotAtIndex(2, ItemStack.of(ItemId.SHOVEL));

        assertEquals(6f, InventoryWeight.totalWeight(inventory, null), 0.001f);
    }

    @Test
    void normalItems_stackOfFour_equalsHalfUnit() {
        PlayerInventory inventory = TestInventories.empty();
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.DIRT_CLUMP, 4));

        assertEquals(0.5f, InventoryWeight.totalWeight(inventory, null), 0.001f);
    }

    @Test
    void largeItems_stackOfFour_equalsOneUnit() {
        PlayerInventory inventory = TestInventories.empty();
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.CRATE, 4));

        assertEquals(1f, InventoryWeight.totalWeight(inventory, null), 0.001f);
    }

    @Test
    void startingInventory_isBelowBurdenThreshold() {
        PlayerInventory inventory = new PlayerInventory();

        assertEquals(12.25f, InventoryWeight.totalWeight(inventory, new EquipmentInventory()), 0.01f);
    }
}
