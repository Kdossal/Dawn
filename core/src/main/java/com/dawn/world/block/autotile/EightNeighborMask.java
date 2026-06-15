package com.dawn.world.block.autotile;

import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import java.util.Locale;

/** 8-bit neighbor mask: cardinals match {@link CardinalMask}; diagonals N=8 world Y+. */
public final class EightNeighborMask {
    public static final int WEST = CardinalMask.WEST;
    public static final int SOUTH = CardinalMask.SOUTH;
    public static final int EAST = CardinalMask.EAST;
    public static final int NORTH = CardinalMask.NORTH;
    public static final int NORTH_EAST = 16;
    public static final int SOUTH_EAST = 32;
    public static final int SOUTH_WEST = 64;
    public static final int NORTH_WEST = 128;
    public static final int ALL = 255;

    private EightNeighborMask() {}

    public static int compute(World world, int x, int y, Layer layer, BlockId sameBlock) {
        int mask = CardinalMask.compute(world, x, y, layer, sameBlock);
        if (hasSame(world, x + 1, y + 1, layer, sameBlock)) {
            mask |= NORTH_EAST;
        }
        if (hasSame(world, x + 1, y - 1, layer, sameBlock)) {
            mask |= SOUTH_EAST;
        }
        if (hasSame(world, x - 1, y - 1, layer, sameBlock)) {
            mask |= SOUTH_WEST;
        }
        if (hasSame(world, x - 1, y + 1, layer, sameBlock)) {
            mask |= NORTH_WEST;
        }
        return mask;
    }

    public static int bitForName(String name) {
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "W" -> WEST;
            case "S" -> SOUTH;
            case "E" -> EAST;
            case "N" -> NORTH;
            case "NE" -> NORTH_EAST;
            case "SE" -> SOUTH_EAST;
            case "SW" -> SOUTH_WEST;
            case "NW" -> NORTH_WEST;
            default -> throw new IllegalArgumentException("Unknown neighbor name: " + name);
        };
    }

    public static int maskFromNames(String[] names) {
        int mask = 0;
        for (String name : names) {
            mask |= bitForName(name);
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
