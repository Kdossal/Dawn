package com.dawn.world.storage;

import com.dawn.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Fixed 4×3 item storage backing for a single crate cell. */
public final class CrateStorage {
    public static final int COLS = 4;
    public static final int ROWS = 3;
    public static final int SLOT_COUNT = COLS * ROWS;

    private final ItemStack[] slots = new ItemStack[SLOT_COUNT];

    public CrateStorage() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = ItemStack.empty();
        }
    }

    public ItemStack[] backingArray() {
        return slots;
    }

    public ItemStack getSlotAtIndex(int index) {
        if (index < 0 || index >= SLOT_COUNT) {
            return ItemStack.empty();
        }
        return slots[index].copy();
    }

    public void setSlotAtIndex(int index, ItemStack stack) {
        if (index < 0 || index >= SLOT_COUNT) {
            return;
        }
        slots[index] = stack == null || stack.isEmpty() ? ItemStack.empty() : stack.copy();
    }

    public static int toIndex(int row, int col) {
        row = Math.max(0, Math.min(row, ROWS - 1));
        col = Math.max(0, Math.min(col, COLS - 1));
        return row * COLS + col;
    }

    /** Copies and returns every non-empty slot (for break drops). */
    public List<ItemStack> drainNonEmptySlots() {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack slot : slots) {
            if (!slot.isEmpty()) {
                out.add(slot.copy());
            }
        }
        return out;
    }
}
