package com.dawn.gameplay;

import com.dawn.config.GameConfig;
import com.dawn.entity.EntityBounds;
import com.dawn.entity.EntityDef;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;

/**
 * Interaction reach from the movement hitbox center (not feet). Radius 1–2 use a plus / extended-plus
 * tile pattern; radius 3+ uses a Euclidean disk so cardinals at (±R, 0) and (0, ±R) are included.
 */
public final class ReachResolver {
    private static final float REACH_EPS = 1e-4f;

    private ReachResolver() {}

    public static int radiusCellsForHeld(ItemStack held) {
        return Math.round(radiusCellsFloatForHeld(held));
    }

    @Deprecated
    public static float radiusForHeld(ItemStack held) {
        return radiusCellsFloatForHeld(held);
    }

    public static float radiusCellsFloatForHeld(ItemStack held) {
        GameConfig cfg = GameConfig.get();
        if (held == null || held.isEmpty()) {
            return cfg.reachDefault;
        }
        ItemDef def = ItemRegistry.get(held);
        if (def == null) {
            return cfg.reachDefault;
        }
        if (def.isTool() && def.reachCells() > 0) {
            return def.reachCells();
        }
        return cfg.reachDefault;
    }

    /** Movement hitbox center in cell space (matches debug reach ring). */
    public static float[] reachCenter(EntityDef def, float feetX, float feetY) {
        EntityBounds bounds = EntityBounds.fromFeet(def, feetX, feetY, 0, 0);
        return new float[] {bounds.moveCenterX(), bounds.moveCenterY()};
    }

    public static boolean inReach(EntityDef def, float feetX, float feetY, int cellX, int cellY, float radiusCells) {
        float[] center = reachCenter(def, feetX, feetY);
        return inReachFromCenter(center[0], center[1], cellX, cellY, Math.round(radiusCells));
    }

    public static boolean inReachFromCenter(float centerX, float centerY, int cellX, int cellY, int radiusCells) {
        if (radiusCells <= 0) {
            int cx = (int) Math.floor(centerX);
            int cy = (int) Math.floor(centerY);
            return cellX == cx && cellY == cy;
        }

        int ox = cellOffsetX(centerX, cellX);
        int oy = cellOffsetY(centerY, cellY);

        return switch (radiusCells) {
            case 1 -> isPlus(ox, oy, 1);
            case 2 -> isPlus(ox, oy, 1) || isCardinalOnly(ox, oy, 2);
            default -> isDisk(centerX, centerY, cellX, cellY, radiusCells);
        };
    }

    /** Axis-aligned plus through {@code arm} cells (no diagonals). */
    private static boolean isPlus(int ox, int oy, int arm) {
        return (ox == 0 && Math.abs(oy) <= arm) || (oy == 0 && Math.abs(ox) <= arm);
    }

    /** Straight lines only at exactly {@code dist} cells. */
    private static boolean isCardinalOnly(int ox, int oy, int dist) {
        return (ox == 0 && Math.abs(oy) == dist) || (oy == 0 && Math.abs(ox) == dist);
    }

    private static boolean isDisk(float centerX, float centerY, int cellX, int cellY, int radius) {
        // Anchor disk to the cell containing the hitbox center so cardinals at ±R are symmetric
        // when the player stands in a tile (hitbox center drifts slightly within the cell).
        float refX = (int) Math.floor(centerX) + 0.5f;
        float refY = (int) Math.floor(centerY) + 0.5f;
        float cellCenterX = cellX + 0.5f;
        float cellCenterY = cellY + 0.5f;
        float dx = cellCenterX - refX;
        float dy = cellCenterY - refY;
        float r = radius;
        return dx * dx + dy * dy <= r * r + REACH_EPS;
    }

    private static int cellOffsetX(float centerX, int cellX) {
        return cellX - (int) Math.floor(centerX);
    }

    private static int cellOffsetY(float centerY, int cellY) {
        return cellY - (int) Math.floor(centerY);
    }
}
