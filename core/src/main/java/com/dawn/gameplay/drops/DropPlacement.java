package com.dawn.gameplay.drops;

import com.dawn.world.World;
import com.dawn.world.block.SurfaceRules;

/** Picks a reachable world position for a newly spawned drop. */
public final class DropPlacement {
    private static final int[][] NEIGHBOR_OFFSETS = {
        {0, 0},
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
        {2, 0}, {-2, 0}, {0, 2}, {0, -2},
    };

    private DropPlacement() {}

    /** Cell-center coordinates in world cells, preferring walkable tiles near the source cell. */
    public static float[] cellCenter(World world, int cellX, int cellY) {
        for (int[] offset : NEIGHBOR_OFFSETS) {
            int x = cellX + offset[0];
            int y = cellY + offset[1];
            if (world.inBounds(x, y) && SurfaceRules.canWalk(world, x, y)) {
                return new float[] {x + 0.5f, y + 0.5f};
            }
        }
        return new float[] {cellX + 0.5f, cellY + 0.5f};
    }
}
