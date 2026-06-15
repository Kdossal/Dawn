package com.dawn.world;

import com.dawn.world.block.BlockId;

/** Test/playground map layouts. */
public final class WorldMaps {
    private WorldMaps() {}

    private static void placeBushes(
            World world,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int stepX,
            int stepY,
            int offsetY) {
        for (int x = minX; x < maxX; x += stepX) {
            for (int y = minY + offsetY; y < maxY; y += stepY) {
                if (!world.inBounds(x, y)) {
                    continue;
                }
                if (world.getFloor(x, y) == BlockId.GRASS && world.getObject(x, y) == BlockId.AIR) {
                    world.setObject(x, y, BlockId.BUSH);
                }
            }
        }
    }

    public static void fillPlayground(World world) {
        int width = world.getWidth();
        int height = world.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world.setGround(x, y, BlockId.DIRT);
                world.setFloor(x, y, BlockId.GRASS);
                world.setObject(x, y, BlockId.AIR);
            }
        }

        for (int x = 0; x < width; x++) {
            world.setFloor(x, 0, BlockId.AIR);
            world.setFloor(x, height - 1, BlockId.AIR);
        }
        for (int y = 0; y < height; y++) {
            world.setFloor(0, y, BlockId.AIR);
            world.setFloor(width - 1, y, BlockId.AIR);
        }

        for (int x = 10; x < 25; x++) {
            for (int y = 10; y < 25; y++) {
                world.setGround(x, y, BlockId.SAND);
                world.setFloor(x, y, BlockId.AIR);
            }
        }

        for (int x = 30; x < 45; x++) {
            for (int y = 30; y < 38; y++) {
                world.setGround(x, y, BlockId.WATER);
                world.setFloor(x, y, BlockId.AIR);
            }
        }

        for (int x = 55; x < 65; x++) {
            for (int y = 55; y < 65; y++) {
                world.setGround(x, y, BlockId.WATER);
                world.setFloor(x, y, BlockId.AIR);
            }
        }

        for (int x = 48; x < 58; x++) {
            for (int y = 42; y < 52; y++) {
                world.setGround(x, y, BlockId.STONE);
                world.setFloor(x, y, BlockId.AIR);
            }
        }

        for (int x = 70; x < 85; x += 3) {
            for (int y = 70; y < 85; y += 4) {
                if (world.inBounds(x, y + 1)) {
                    world.setObject(x, y, BlockId.ROCK);
                }
            }
        }

        placeBushes(world, 5, 40, 5, 40, 3, 4, 0);
        placeBushes(world, 7, 38, 7, 38, 3, 4, 2);
        placeBushes(world, 42, 68, 8, 38, 4, 5, 1);

        int[][] oakSpots = {{20, 60}, {25, 61}, {80, 20}, {82, 21}};
        for (int[] spot : oakSpots) {
            if (world.inBounds(spot[0], spot[1])) {
                world.setObject(spot[0], spot[1], BlockId.OAK_TREE);
            }
        }

        int[][] spruceSpots = {{15, 55}, {18, 56}, {72, 75}, {74, 76}};
        for (int[] spot : spruceSpots) {
            if (world.inBounds(spot[0], spot[1])) {
                world.setObject(spot[0], spot[1], BlockId.SPRUCE_TREE);
            }
        }
    }
}
