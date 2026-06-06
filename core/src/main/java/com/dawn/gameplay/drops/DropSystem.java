package com.dawn.gameplay.drops;

import com.dawn.config.GameConfig;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;
import com.dawn.entity.Entity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DropSystem {
    private final List<WorldDrop> drops = new ArrayList<>();

    public List<WorldDrop> getDrops() {
        return drops;
    }

    public void update(float delta) {
        for (WorldDrop drop : drops) {
            if (drop.pickupCooldown > 0f) {
                drop.pickupCooldown = Math.max(0f, drop.pickupCooldown - delta);
            }
        }
    }

    /** Loot from breaking / world sources — immediately pickable. */
    public WorldDrop spawn(ItemStack stack, float x, float y) {
        return spawn(stack, x, y, 0f);
    }

    public void spawnAtCell(ItemStack stack, int cellX, int cellY) {
        spawn(stack, cellX, cellY);
    }

    /** Q-drop, inventory drag-out, etc. — short pickup cooldown. */
    public WorldDrop spawnPlayerDrop(ItemStack stack, float x, float y) {
        return spawn(stack, x, y, GameConfig.get().pickupCooldownSec);
    }

    private WorldDrop spawn(ItemStack stack, float x, float y, float pickupCooldown) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        float scatter = GameConfig.get().dropScatterCells;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        float sx = x + 0.5f + (rng.nextFloat() * 2f - 1f) * scatter;
        float sy = y + 0.5f + (rng.nextFloat() * 2f - 1f) * scatter;
        WorldDrop drop = new WorldDrop(stack, sx, sy, pickupCooldown);
        drops.add(drop);
        return drop;
    }

    public void tryPickupAll(Entity entity, PlayerInventory inventory) {
        GameConfig cfg = GameConfig.get();
        float px = entity.getX();
        float py = entity.getY();
        Iterator<WorldDrop> it = drops.iterator();
        while (it.hasNext()) {
            WorldDrop drop = it.next();
            if (drop.pickupCooldown > 0f) {
                continue;
            }
            float dx = drop.x - px;
            float dy = drop.y - py;
            float dist = Math.max(Math.abs(dx), Math.abs(dy));
            if (dist <= cfg.pickupRadiusCells) {
                int left = inventory.tryAdd(drop.stack);
                if (left <= 0) {
                    it.remove();
                } else if (left < drop.stack.count) {
                    drop.stack = ItemStack.of(drop.stack.itemId, left);
                }
            }
        }
    }

    public void dropFromEntity(Entity entity, PlayerInventory inventory, boolean fullStack) {
        ItemStack held = inventory.getHeld();
        if (held.isEmpty()) {
            return;
        }
        int amount = fullStack ? held.count : 1;
        ItemStack extracted = inventory.extractFromHeld(amount);
        if (!extracted.isEmpty()) {
            spawnPlayerDrop(extracted, entity.getX(), entity.getY());
        }
    }
}
