package com.dawn.item;

/** Stack cap and per-item carry weight for bulk goods. */
public enum ItemWeightTier {
    VERY_SMALL(32, 1f / 32f),
    SMALL(16, 1f / 16f),
    NORMAL(8, 1f / 8f),
    LARGE(4, 1f / 4f);

    public final int maxStack;
    public final float weightPerItem;

    ItemWeightTier(int maxStack, float weightPerItem) {
        this.maxStack = maxStack;
        this.weightPerItem = weightPerItem;
    }
}
