package com.dawn.gameplay.drops;

import com.dawn.item.ItemStack;

public final class WorldDrop {
    private static int nextId = 1;

    public final int id;
    public ItemStack stack;
    public float x;
    public float y;
    public float pickupCooldown;

    public WorldDrop(ItemStack stack, float x, float y, float pickupCooldown) {
        this.id = nextId++;
        this.stack = stack.copy();
        this.x = x;
        this.y = y;
        this.pickupCooldown = pickupCooldown;
    }
}
