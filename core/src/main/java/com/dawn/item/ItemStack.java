package com.dawn.item;

public final class ItemStack {
    public final ItemId itemId;
    public final int count;

    public ItemStack(ItemId itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }

    public static ItemStack empty() {
        return new ItemStack(null, 0);
    }

    public static ItemStack of(ItemId itemId, int count) {
        return new ItemStack(itemId, count);
    }

    public static ItemStack of(ItemId itemId) {
        return of(itemId, 1);
    }

    public boolean isEmpty() {
        return itemId == null || count <= 0;
    }

    public ItemStack copy() {
        return isEmpty() ? empty() : new ItemStack(itemId, count);
    }

    public ItemStack withCount(int newCount) {
        if (newCount <= 0) {
            return empty();
        }
        return new ItemStack(itemId, newCount);
    }

    public boolean canMergeWith(ItemStack other) {
        return !isEmpty()
                && other != null
                && !other.isEmpty()
                && itemId == other.itemId;
    }

    public int spaceLeftInStack() {
        if (isEmpty()) {
            return 0;
        }
        ItemDef def = ItemRegistry.get(itemId);
        return Math.max(0, def.maxStack() - count);
    }
}
