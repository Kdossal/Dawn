package com.dawn.inventory;

import com.dawn.item.ItemId;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.item.ItemDef;
import java.util.ArrayList;
import java.util.List;

public final class PlayerInventory {
    /** Default held slot: shovel in the right-hand tool group (grid row 2, col 2). */
    private static final int DEFAULT_SELECTED_INDEX = toIndex(2, 2);

    private final ItemStack[] slots = new ItemStack[InventoryConstants.SIZE];
    private int selectedIndex = DEFAULT_SELECTED_INDEX;

    public PlayerInventory() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = ItemStack.empty();
        }
        setSlotAtIndex(10, ItemStack.of(ItemId.HAMMER));
        setSlotAtIndex(11, ItemStack.of(ItemId.SAW));
        setSlotAtIndex(12, ItemStack.of(ItemId.SHOVEL));
        setSlotAtIndex(5, ItemStack.of(ItemId.LANTERN, 4));
        setSlotAtIndex(6, ItemStack.of(ItemId.SPRUCE_SAPLING, 4));
        setSlotAtIndex(7, ItemStack.of(ItemId.LOG, 8));
        setSlotAtIndex(8, ItemStack.of(ItemId.CLOTH, 8));
        setSlotAtIndex(0, ItemStack.of(ItemId.LEATHER_HOOD));
        setSlotAtIndex(1, ItemStack.of(ItemId.CANNED_FOOD, 4));
    }

    public ItemStack[] backingArray() {
        return slots;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < InventoryConstants.SIZE) {
            selectedIndex = index;
        }
    }

    public void cycleSelectedIndex(int delta) {
        selectedIndex = Math.floorMod(selectedIndex + delta, InventoryConstants.SIZE);
    }

    public ItemStack getHeld() {
        return getSlotAtIndex(selectedIndex);
    }

    public int getHeldIndex() {
        return selectedIndex;
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
        order.add(getHeldIndex());
        for (int idx = 0; idx < InventoryConstants.SIZE; idx++) {
            if (idx != getHeldIndex()) {
                order.add(idx);
            }
        }
        return order;
    }

    public static int toIndex(int row, int col) {
        row = Math.max(0, Math.min(row, InventoryConstants.ROWS - 1));
        col = Math.max(0, Math.min(col, InventoryConstants.COLS - 1));
        return row * InventoryConstants.COLS + col;
    }

    public static int rowFromIndex(int index) {
        return index / InventoryConstants.COLS;
    }

    public static int colFromIndex(int index) {
        return index % InventoryConstants.COLS;
    }
}
