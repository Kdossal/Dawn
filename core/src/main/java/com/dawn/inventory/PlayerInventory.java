package com.dawn.inventory;

import com.dawn.item.ItemId;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.item.ItemDef;
import java.util.ArrayList;
import java.util.List;

public final class PlayerInventory {
    private final ItemStack[] slots = new ItemStack[InventoryConstants.SIZE];
    private int activeRow = InventoryConstants.HOTBAR_ROW;
    private int selectedCol = 2;

    public PlayerInventory() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = ItemStack.empty();
        }
        setSlot(InventoryConstants.HOTBAR_ROW, 0, ItemStack.of(ItemId.PICKAXE));
        setSlot(InventoryConstants.HOTBAR_ROW, 1, ItemStack.of(ItemId.AXE));
        setSlot(InventoryConstants.HOTBAR_ROW, 2, ItemStack.of(ItemId.SHOVEL));
        setSlot(InventoryConstants.HOTBAR_ROW, 3, ItemStack.of(ItemId.CRATE, 4));
        setSlot(InventoryConstants.HOTBAR_ROW, 4, ItemStack.of(ItemId.BED, 4));
        setSlot(InventoryConstants.HOTBAR_ROW, 5, ItemStack.of(ItemId.LANTERN, 4));
        setSlot(InventoryConstants.HOTBAR_ROW, 6, ItemStack.of(ItemId.SPRUCE_SAPLING, 4));
        setSlot(InventoryConstants.HOTBAR_ROW, 7, ItemStack.of(ItemId.DIRT_CLUMP, 4));
        setSlot(InventoryConstants.HOTBAR_ROW, 8, ItemStack.of(ItemId.STONE, 8));
        setSlot(InventoryConstants.HOTBAR_ROW, 9, ItemStack.of(ItemId.STONE, 8));
        setSlot(0, 0, ItemStack.of(ItemId.LEATHER_HOOD));
        setSlot(0, 1, ItemStack.of(ItemId.CANNED_FOOD, 4));
    }

    public ItemStack[] backingArray() {
        return slots;
    }

    public int getActiveRow() {
        return activeRow;
    }

    public void setActiveRow(int row) {
        if (row >= 0 && row < InventoryConstants.ROWS) {
            activeRow = row;
        }
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void setSelectedCol(int col) {
        if (col >= 0 && col < InventoryConstants.COLS) {
            selectedCol = col;
        }
    }

    public void cycleRow(int delta) {
        activeRow = Math.floorMod(activeRow + delta, InventoryConstants.ROWS);
    }

    public ItemStack getHeld() {
        return getSlot(activeRow, selectedCol);
    }

    public int getHeldIndex() {
        return toIndex(activeRow, selectedCol);
    }

    public ItemStack getSlot(int row, int col) {
        return slots[toIndex(row, col)].copy();
    }

    public ItemStack getSlotAtIndex(int index) {
        return slots[index].copy();
    }

    public void setSlot(int row, int col, ItemStack stack) {
        setSlotAtIndex(toIndex(row, col), stack);
    }

    public void setSlotAtIndex(int index, ItemStack stack) {
        slots[index] = stack == null || stack.isEmpty() ? ItemStack.empty() : stack.copy();
    }

    public void swapSlots(int indexA, int indexB) {
        InventoryOperations.swap(slots, indexA, indexB);
    }

    public boolean moveSlot(int fromIndex, int toIndex) {
        return InventoryOperations.moveStack(slots, fromIndex, toIndex);
    }

    public ItemStack[] getActiveRowSlots() {
        ItemStack[] row = new ItemStack[InventoryConstants.COLS];
        for (int c = 0; c < InventoryConstants.COLS; c++) {
            row[c] = getSlot(activeRow, c);
        }
        return row;
    }

    public int tryAdd(ItemStack incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return 0;
        }
        int remaining = incoming.count;
        List<Integer> order = pickupSlotOrder();
        for (int idx : order) {
            ItemStack slot = slots[idx];
            if (!slot.isEmpty() && slot.itemId == incoming.itemId) {
                ItemDef def = ItemRegistry.get(incoming.itemId);
                int space = def.maxStack() - slot.count;
                if (space > 0) {
                    int add = Math.min(space, remaining);
                    slots[idx] = slot.withCount(slot.count + add);
                    remaining -= add;
                    if (remaining <= 0) {
                        return 0;
                    }
                }
            }
        }
        for (int idx : order) {
            if (slots[idx].isEmpty()) {
                ItemDef def = ItemRegistry.get(incoming.itemId);
                int add = Math.min(def.maxStack(), remaining);
                slots[idx] = ItemStack.of(incoming.itemId, add);
                remaining -= add;
                if (remaining <= 0) {
                    return 0;
                }
            }
        }
        return remaining;
    }

    public int removeFromHeld(int amount) {
        int idx = getHeldIndex();
        ItemStack slot = slots[idx];
        if (slot.isEmpty() || amount <= 0) {
            return 0;
        }
        int removed = Math.min(amount, slot.count);
        int left = slot.count - removed;
        slots[idx] = left <= 0 ? ItemStack.empty() : slot.withCount(left);
        return removed;
    }

    public ItemStack extractFromHeld(int amount) {
        int idx = getHeldIndex();
        ItemStack slot = slots[idx];
        if (slot.isEmpty() || amount <= 0) {
            return ItemStack.empty();
        }
        int removed = Math.min(amount, slot.count);
        int left = slot.count - removed;
        slots[idx] = left <= 0 ? ItemStack.empty() : slot.withCount(left);
        return ItemStack.of(slot.itemId, removed);
    }

    public ItemStack extractFromIndex(int index, int amount) {
        ItemStack slot = slots[index];
        if (slot.isEmpty() || amount <= 0) {
            return ItemStack.empty();
        }
        int removed = Math.min(amount, slot.count);
        int left = slot.count - removed;
        slots[index] = left <= 0 ? ItemStack.empty() : slot.withCount(left);
        return ItemStack.of(slot.itemId, removed);
    }

    private List<Integer> pickupSlotOrder() {
        List<Integer> order = new ArrayList<>(InventoryConstants.SIZE);
        for (int c = 0; c < InventoryConstants.COLS; c++) {
            order.add(toIndex(activeRow, c));
        }
        for (int r = 0; r < InventoryConstants.ROWS; r++) {
            if (r == activeRow) {
                continue;
            }
            for (int c = 0; c < InventoryConstants.COLS; c++) {
                order.add(toIndex(r, c));
            }
        }
        return order;
    }

    public static int toIndex(int row, int col) {
        return row * InventoryConstants.COLS + col;
    }

    public static int rowFromIndex(int index) {
        return index / InventoryConstants.COLS;
    }

    public static int colFromIndex(int index) {
        return index % InventoryConstants.COLS;
    }
}
