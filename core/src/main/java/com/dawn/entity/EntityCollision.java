package com.dawn.entity;

import com.dawn.world.World;

/** Movement footprint vs tile grid (half-open cell overlap). */
public final class EntityCollision {
    public static final float CELL_EPS = 1e-5f;
    public static final float EPSILON = 1e-4f;

    private EntityCollision() {}

    public static int cellMin(float worldCoord) {
        return (int) Math.floor(worldCoord + CELL_EPS);
    }

    public static int cellMax(float worldCoord) {
        return (int) Math.floor(worldCoord - CELL_EPS);
    }

    public static boolean overlapsSolid(EntityBounds box, World world) {
        int minCellX = cellMin(box.moveLeft);
        int maxCellX = cellMax(box.moveRight);
        int minCellY = cellMin(box.moveBottom);
        int maxCellY = cellMax(box.moveTop);

        for (int cy = minCellY; cy <= maxCellY; cy++) {
            for (int cx = minCellX; cx <= maxCellX; cx++) {
                if (overlapsCell(box, cx, cy) && world.isSolidForMovement(cx, cy)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean overlapsCell(EntityBounds box, int cellX, int cellY) {
        float cellLeft = cellX;
        float cellRight = cellX + 1f;
        float cellBottom = cellY;
        float cellTop = cellY + 1f;
        return box.moveRight > cellLeft + CELL_EPS
                && box.moveLeft < cellRight - CELL_EPS
                && box.moveTop > cellBottom + CELL_EPS
                && box.moveBottom < cellTop - CELL_EPS;
    }

}
