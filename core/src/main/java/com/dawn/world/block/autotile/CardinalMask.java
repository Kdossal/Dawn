package com.dawn.world.block.autotile;

import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;

/** Builds a 4-bit cardinal neighbor mask (N=8, E=4, S=2, W=1). */
public final class CardinalMask {
    public static final int NORTH = 8;
    public static final int EAST = 4;
    public static final int SOUTH = 2;
    public static final int WEST = 1;

    private CardinalMask() {}

    public static int compute(World world, int x, int y, Layer layer, BlockId sameBlock) {
        int mask = 0;
        if (hasSame(world, x, y + 1, layer, sameBlock)) {
            mask |= NORTH;
        }
        if (hasSame(world, x + 1, y, layer, sameBlock)) {
            mask |= EAST;
        }
        if (hasSame(world, x, y - 1, layer, sameBlock)) {
            mask |= SOUTH;
        }
        if (hasSame(world, x - 1, y, layer, sameBlock)) {
            mask |= WEST;
        }
        return mask;
    }

    private static boolean hasSame(World world, int x, int y, Layer layer, BlockId sameBlock) {
        if (!world.inBounds(x, y)) {
            return false;
        }
        return blockAt(world, x, y, layer) == sameBlock;
    }

    private static BlockId blockAt(World world, int x, int y, Layer layer) {
        return switch (layer) {
            case GROUND -> world.getGround(x, y);
            case FLOOR -> world.getFloor(x, y);
            case OBJECT -> world.getObject(x, y);
        };
    }
}
