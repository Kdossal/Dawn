package com.dawn.inventory;

import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;

/** Merge, swap, and move logic shared by grid and equipment slots. */
public final class InventoryOperations {
    private InventoryOperations() {}

    public static void swap(ItemStack[] slots, int indexA, int indexB) {
        ItemStack tmp = slots[indexA].copy();
        slots[indexA] = slots[indexB].copy();
        slots[indexB] = tmp;
    }

    /**
     * Move from source to target (merge or swap). Returns true if anything changed.
     */
    public static boolean moveStack(ItemStack[] slots, int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
            return false;
        }
        ItemStack from = slots[fromIndex];
        ItemStack to = slots[toIndex];
        if (from.isEmpty()) {
            return false;
        }
        if (to.isEmpty()) {
            slots[toIndex] = from.copy();
            slots[fromIndex] = ItemStack.empty();
            return true;
        }
        if (from.itemId == to.itemId) {
            ItemDef def = ItemRegistry.get(from.itemId);
            int space = def.maxStack() - to.count;
            if (space <= 0) {
                return false;
            }
            int move = Math.min(space, from.count);
            slots[toIndex] = to.withCount(to.count + move);
            int left = from.count - move;
            slots[fromIndex] = left <= 0 ? ItemStack.empty() : from.withCount(left);
            return true;
        }
        swap(slots, fromIndex, toIndex);
        return true;
    }
}
