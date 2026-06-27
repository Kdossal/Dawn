package com.dawn.gameplay;

import com.dawn.entity.Entity;
import com.dawn.item.ItemStack;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;

/** Finds nearest in-range interactable world targets for key-based interaction. */
public final class InteractResolver {
    private InteractResolver() {}

    public record InteractTarget(int cellX, int cellY) {}

    /** Returns whether a crate at {@code (cellX, cellY)} is within player reach. */
    public static boolean isCrateInReach(World world, Entity player, int cellX, int cellY) {
        if (world == null || player == null || !world.inBounds(cellX, cellY)) {
            return false;
        }
        if (world.getObject(cellX, cellY) != BlockId.CRATE) {
            return false;
        }
        float radiusFloat = ReachResolver.radiusCellsFloatForHeld(ItemStack.empty());
        float[] center = ReachResolver.reachCenter(player.def(), player.getX(), player.getY());
        return ReachResolver.inReachFromCenter(center[0], center[1], cellX, cellY, Math.max(0, Math.round(radiusFloat)));
    }

    /** Step 1: nearest crate in reach. */
    public static InteractTarget nearestCrateInReach(World world, Entity player) {
        if (world == null || player == null) {
            return null;
        }
        float radiusFloat = ReachResolver.radiusCellsFloatForHeld(ItemStack.empty());
        int radius = Math.max(0, Math.round(radiusFloat));
        float[] center = ReachResolver.reachCenter(player.def(), player.getX(), player.getY());
        int centerCellX = (int) Math.floor(center[0]);
        int centerCellY = (int) Math.floor(center[1]);

        InteractTarget best = null;
        float bestDistSq = Float.MAX_VALUE;
        for (int y = centerCellY - radius; y <= centerCellY + radius; y++) {
            for (int x = centerCellX - radius; x <= centerCellX + radius; x++) {
                if (!world.inBounds(x, y)) {
                    continue;
                }
                if (!ReachResolver.inReachFromCenter(center[0], center[1], x, y, radius)) {
                    continue;
                }
                if (world.getObject(x, y) != BlockId.CRATE) {
                    continue;
                }
                float dx = (x + 0.5f) - center[0];
                float dy = (y + 0.5f) - center[1];
                float distSq = dx * dx + dy * dy;
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = new InteractTarget(x, y);
                }
            }
        }
        return best;
    }
}
