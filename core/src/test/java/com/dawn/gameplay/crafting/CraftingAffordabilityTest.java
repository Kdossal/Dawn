package com.dawn.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import org.junit.jupiter.api.Test;

class CraftingAffordabilityTest {
    @Test
    void countInGrid_sumsAcrossSlots() {
        PlayerInventory inv = new PlayerInventory();
        inv.setSlot(1, 2, ItemStack.of(ItemId.LOG, 3));
        inv.setSlot(1, 3, ItemStack.of(ItemId.LOG, 5));
        assertEquals(8, CraftingAffordability.countInGrid(inv, ItemId.LOG));
    }

    @Test
    void canAfford_bandageRecipe() {
        PlayerInventory inv = new PlayerInventory();
        Recipe bandage = RecipeRegistry.get(RecipeId.BANDAGE);
        assertTrue(CraftingAffordability.canAfford(bandage, inv));
    }

    @Test
    void canAfford_failsWhenShort() {
        PlayerInventory inv = new PlayerInventory();
        inv.setSlot(1, 3, ItemStack.empty());
        Recipe bandage = RecipeRegistry.get(RecipeId.BANDAGE);
        assertFalse(CraftingAffordability.canAfford(bandage, inv));
    }

    @Test
    void consumeCosts_removesFromGrid() {
        PlayerInventory inv = new PlayerInventory();
        int clothBefore = CraftingAffordability.countInGrid(inv, ItemId.CLOTH);
        Recipe bandage = RecipeRegistry.get(RecipeId.BANDAGE);
        assertTrue(CraftingAffordability.consumeCosts(bandage, inv));
        assertEquals(clothBefore - 2, CraftingAffordability.countInGrid(inv, ItemId.CLOTH));
    }

    @Test
    void consumeCosts_failsSafelyWhenInsufficient() {
        PlayerInventory inv = new PlayerInventory();
        inv.setSlot(1, 3, ItemStack.empty());
        int clothBefore = CraftingAffordability.countInGrid(inv, ItemId.CLOTH);
        Recipe bandage = RecipeRegistry.get(RecipeId.BANDAGE);
        assertFalse(CraftingAffordability.consumeCosts(bandage, inv));
        assertEquals(clothBefore, CraftingAffordability.countInGrid(inv, ItemId.CLOTH));
    }
}
