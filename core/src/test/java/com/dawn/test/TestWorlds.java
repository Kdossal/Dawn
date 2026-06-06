package com.dawn.test;

import com.dawn.world.World;
import com.dawn.world.block.BlockId;

/** Minimal worlds for unit tests. */
public final class TestWorlds {
    private TestWorlds() {}

    public static World smallClear(int width, int height) {
        World world = new World(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world.setGround(x, y, BlockId.PIT);
                world.setFloor(x, y, BlockId.AIR);
                world.setObject(x, y, BlockId.AIR);
            }
        }
        return world;
    }

    /** Walkable field: dirt ground, no floor or object. */
    public static World smallWalkable(int width, int height) {
        World world = smallClear(width, height);
        fillDirtGround(world);
        return world;
    }

    public static void fillDirtGround(World world) {
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                world.setGround(x, y, BlockId.DIRT);
                world.setFloor(x, y, BlockId.AIR);
                world.setObject(x, y, BlockId.AIR);
            }
        }
    }

    /** Pit cell with solid dirt ground (no floor overlay). */
    public static void setSolidDirt(World world, int x, int y) {
        world.setGround(x, y, BlockId.DIRT);
        world.setFloor(x, y, BlockId.AIR);
        world.setObject(x, y, BlockId.AIR);
    }
}
