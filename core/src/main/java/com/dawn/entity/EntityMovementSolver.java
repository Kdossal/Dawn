package com.dawn.entity;

import com.dawn.world.World;

/**
 * Resolves feet position against the tile grid: diagonal slide, best axis order, sub-steps for large
 * deltas, depenetration when overlapping solids, and light corridor alignment for 1-cell passages.
 */
public final class EntityMovementSolver {
    /** Max movement per sub-step (cells); avoids tunneling through thin walls at high speed. */
    private static final float MAX_STEP_CELLS = 0.5f;

    private static final int MAX_DEPENETRATION_ITERATIONS = 6;

    private EntityMovementSolver() {}

    public record Result(float feetX, float feetY, boolean moved) {}

    public static Result move(EntityDef def, float feetX, float feetY, float dx, float dy, World world) {
        if (dx == 0f && dy == 0f) {
            return new Result(feetX, feetY, false);
        }

        float[] sep = separateFromSolids(def, feetX, feetY, world);
        float[] separated = clampFeet(def, sep[0], sep[1], world);
        feetX = separated[0];
        feetY = separated[1];

        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= MAX_STEP_CELLS) {
            return moveOnce(def, feetX, feetY, dx, dy, world);
        }

        float stepX = dx * (MAX_STEP_CELLS / len);
        float stepY = dy * (MAX_STEP_CELLS / len);
        int steps = (int) Math.ceil(len / MAX_STEP_CELLS);
        boolean moved = false;
        float usedX = 0f;
        float usedY = 0f;
        for (int i = 0; i < steps; i++) {
            float sx = i == steps - 1 ? dx - usedX : stepX;
            float sy = i == steps - 1 ? dy - usedY : stepY;
            usedX += sx;
            usedY += sy;
            Result step = moveOnce(def, feetX, feetY, sx, sy, world);
            feetX = step.feetX();
            feetY = step.feetY();
            moved |= step.moved();
        }
        return resultAt(clampFeet(def, feetX, feetY, world), moved);
    }

    private static Result moveOnce(EntityDef def, float feetX, float feetY, float dx, float dy, World world) {
        float goalX = feetX + dx;
        float goalY = feetY + dy;

        if (canStandAt(def, goalX, goalY, world)) {
            return resultAt(clampFeet(def, goalX, goalY, world), true);
        }

        float[] xFirst = slideAxes(def, feetX, feetY, dx, dy, world, true);
        float[] yFirst = slideAxes(def, feetX, feetY, dx, dy, world, false);

        float remainX1 = goalX - xFirst[0];
        float remainY1 = goalY - xFirst[1];
        float remainX2 = goalX - yFirst[0];
        float remainY2 = goalY - yFirst[1];
        float err1 = remainX1 * remainX1 + remainY1 * remainY1;
        float err2 = remainX2 * remainX2 + remainY2 * remainY2;

        float[] chosen = err1 <= err2 ? xFirst : yFirst;
        float newX = chosen[0];
        float newY = chosen[1];

        float[] aligned = assistCorridorAlignment(def, newX, newY, dx, dy, world);
        if (aligned[0] != newX || aligned[1] != newY) {
            float[] retry = slideAxes(def, aligned[0], aligned[1], dx, dy, world, err1 <= err2);
            float rErrX = goalX - retry[0];
            float rErrY = goalY - retry[1];
            float aErrX = goalX - aligned[0];
            float aErrY = goalY - aligned[1];
            if (rErrX * rErrX + rErrY * rErrY < aErrX * aErrX + aErrY * aErrY) {
                newX = retry[0];
                newY = retry[1];
            } else {
                newX = aligned[0];
                newY = aligned[1];
            }
        } else {
            newX = aligned[0];
            newY = aligned[1];
        }

        boolean moved = Math.abs(newX - feetX) > EntityCollision.EPSILON || Math.abs(newY - feetY) > EntityCollision.EPSILON;
        return resultAt(clampFeet(def, newX, newY, world), moved);
    }

    private static Result resultAt(float[] feet, boolean moved) {
        return new Result(feet[0], feet[1], moved);
    }

    /** Try full delta on one axis, then the other (wall slide). */
    private static float[] slideAxes(
            EntityDef def, float feetX, float feetY, float dx, float dy, World world, boolean xFirst) {
        float x = feetX;
        float y = feetY;
        if (xFirst) {
            x = tryAxisX(def, x, y, dx, world);
            y = tryAxisY(def, x, y, dy, world);
        } else {
            y = tryAxisY(def, x, y, dy, world);
            x = tryAxisX(def, x, y, dx, world);
        }
        return new float[] {x, y};
    }

    private static float tryAxisX(EntityDef def, float feetX, float feetY, float dx, World world) {
        if (dx == 0f) {
            return feetX;
        }
        float newX = feetX + dx;
        return canStandAt(def, newX, feetY, world) ? newX : feetX;
    }

    private static float tryAxisY(EntityDef def, float feetX, float feetY, float dy, World world) {
        if (dy == 0f) {
            return feetY;
        }
        float newY = feetY + dy;
        return canStandAt(def, feetX, newY, world) ? newY : feetY;
    }

    /**
     * When the footprint fits a 1-cell-wide or 1-cell-tall passage, nudge toward corridor alignment if
     * already close (reduces float drift that blocks entry).
     */
    private static float[] assistCorridorAlignment(
            EntityDef def, float feetX, float feetY, float dx, float dy, World world) {
        float maxPassage = 1f - 2f * EntityCollision.CELL_EPS;
        float snapSlack = 0.12f;

        // Keep corridor snap as a gentle assist for straight-axis motion only.
        if (def.moveWidthCells() <= maxPassage && dy != 0f && Math.abs(dx) <= EntityCollision.EPSILON) {
            int col = (int) Math.floor(feetX + 0.5f);
            float centeredX = col + 0.5f;
            if (Math.abs(feetX - centeredX) <= snapSlack && canStandAt(def, centeredX, feetY, world)) {
                feetX = centeredX;
            }
        }

        if (def.moveHeightCells() <= maxPassage && dx != 0f && Math.abs(dy) <= EntityCollision.EPSILON) {
            int floorRow = (int) Math.floor(feetY + EntityCollision.CELL_EPS);
            if (Math.abs(feetY - floorRow) <= snapSlack && canStandAt(def, feetX, floorRow, world)) {
                feetY = floorRow;
            }
        }

        return new float[] {feetX, feetY};
    }

    /** Push feet out of overlapping solid cells (spawn overlap, corner clip). */
    static float[] separateFromSolids(EntityDef def, float feetX, float feetY, World world) {
        if (!overlapsSolid(def, feetX, feetY, world)) {
            return new float[] {feetX, feetY};
        }

        for (int iter = 0; iter < MAX_DEPENETRATION_ITERATIONS; iter++) {
            EntityBounds box = EntityBounds.fromFeet(def, feetX, feetY, 0, 0);
            if (!EntityCollision.overlapsSolid(box, world)) {
                break;
            }

            float bestPushX = 0f;
            float bestPushY = 0f;
            float minPen = Float.MAX_VALUE;

            int minCellX = EntityCollision.cellMin(box.moveLeft);
            int maxCellX = EntityCollision.cellMax(box.moveRight);
            int minCellY = EntityCollision.cellMin(box.moveBottom);
            int maxCellY = EntityCollision.cellMax(box.moveTop);

            for (int cy = minCellY; cy <= maxCellY; cy++) {
                for (int cx = minCellX; cx <= maxCellX; cx++) {
                    if (!EntityCollision.overlapsCell(box, cx, cy) || !world.isSolidForMovement(cx, cy)) {
                        continue;
                    }
                    float cellLeft = cx;
                    float cellRight = cx + 1f;
                    float cellBottom = cy;
                    float cellTop = cy + 1f;

                    float penLeft = box.moveRight - cellLeft;
                    float penRight = cellRight - box.moveLeft;
                    float penBottom = box.moveTop - cellBottom;
                    float penTop = cellTop - box.moveBottom;

                    if (penLeft > EntityCollision.CELL_EPS && penLeft < minPen) {
                        minPen = penLeft;
                        bestPushX = -penLeft;
                        bestPushY = 0f;
                    }
                    if (penRight > EntityCollision.CELL_EPS && penRight < minPen) {
                        minPen = penRight;
                        bestPushX = penRight;
                        bestPushY = 0f;
                    }
                    if (penBottom > EntityCollision.CELL_EPS && penBottom < minPen) {
                        minPen = penBottom;
                        bestPushX = 0f;
                        bestPushY = -penBottom;
                    }
                    if (penTop > EntityCollision.CELL_EPS && penTop < minPen) {
                        minPen = penTop;
                        bestPushX = 0f;
                        bestPushY = penTop;
                    }
                }
            }

            if (minPen == Float.MAX_VALUE) {
                break;
            }
            feetX += bestPushX;
            feetY += bestPushY;
        }

        return new float[] {feetX, feetY};
    }

    static boolean canStandAt(EntityDef def, float feetX, float feetY, World world) {
        if (!isFeetInWorldBounds(def, feetX, feetY, world)) {
            return false;
        }
        return !overlapsSolid(def, feetX, feetY, world);
    }

    private static boolean overlapsSolid(EntityDef def, float feetX, float feetY, World world) {
        return EntityCollision.overlapsSolid(EntityBounds.fromFeet(def, feetX, feetY, 0, 0), world);
    }

    private static boolean isFeetInWorldBounds(EntityDef def, float feetX, float feetY, World world) {
        float halfW = def.moveWidthCells() / 2f;
        float h = def.moveHeightCells();
        if (feetX - halfW < 0f || feetX + halfW > world.getWidth()) {
            return false;
        }
        return feetY >= 0f && feetY + h <= world.getHeight();
    }

    static float[] clampFeet(EntityDef def, float feetX, float feetY, World world) {
        float halfW = def.moveWidthCells() / 2f;
        float h = def.moveHeightCells();
        float minX = halfW;
        float maxX = world.getWidth() - halfW;
        float maxY = world.getHeight() - h;
        if (maxX < minX) {
            feetX = world.getWidth() / 2f;
        } else {
            feetX = Math.max(minX, Math.min(maxX, feetX));
        }
        if (maxY < 0f) {
            feetY = 0f;
        } else {
            feetY = Math.max(0f, Math.min(maxY, feetY));
        }
        return new float[] {feetX, feetY};
    }
}
