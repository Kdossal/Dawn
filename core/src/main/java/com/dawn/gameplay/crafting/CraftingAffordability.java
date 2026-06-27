package com.dawn.gameplay.crafting;

import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;

/** Read-only and consume helpers for recipe material costs in the player grid. */
public final class CraftingAffordability {
    private CraftingAffordability() {}

    public static int countInGrid(PlayerInventory inventory, ItemId itemId) {
        if (inventory == null || itemId == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < InventoryConstants.SIZE; i++) {
            ItemStack slot = inventory.getSlotAtIndex(i);
            if (!slot.isEmpty() && slot.itemId == itemId) {
                total += slot.count;
            }
        }
        return total;
    }

    public static boolean canAfford(Recipe recipe, PlayerInventory inventory) {
        if (recipe == null || inventory == null) {
            return false;
        }
        for (ItemStack cost : recipe.costs()) {
            if (cost == null || cost.isEmpty()) {
                continue;
            }
            if (countInGrid(inventory, cost.itemId) < cost.count) {
                return false;
            }
        }
        return true;
    }

    public static boolean consumeCosts(Recipe recipe, PlayerInventory inventory) {
        if (!canAfford(recipe, inventory)) {
            return false;
        }
        for (ItemStack cost : recipe.costs()) {
            if (cost == null || cost.isEmpty()) {
                continue;
            }
            removeFromGrid(inventory, cost.itemId, cost.count);
        }
        return true;
    }

    private static void removeFromGrid(PlayerInventory inventory, ItemId itemId, int amount) {
        int remaining = amount;
        int heldIdx = inventory.getHeldIndex();
        remaining = extractFromIndex(inventory, heldIdx, itemId, remaining);
        if (remaining <= 0) {
            return;
        }
        for (int i = 0; i < InventoryConstants.SIZE; i++) {
            if (i == heldIdx) {
                continue;
            }
            remaining = extractFromIndex(inventory, i, itemId, remaining);
            if (remaining <= 0) {
                return;
            }
        }
    }

    private static int extractFromIndex(PlayerInventory inventory, int index, ItemId itemId, int amount) {
        ItemStack slot = inventory.getSlotAtIndex(index);
        if (slot.isEmpty() || slot.itemId != itemId || amount <= 0) {
            return amount;
        }
        int take = Math.min(amount, slot.count);
        int left = slot.count - take;
        inventory.setSlotAtIndex(index, left <= 0 ? ItemStack.empty() : slot.withCount(left));
        return amount - take;
    }
}
