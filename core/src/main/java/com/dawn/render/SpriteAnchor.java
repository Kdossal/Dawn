package com.dawn.render;

import com.dawn.config.Constants;

/** Converts world cell positions to sprite batch pixel coordinates. */
public final class SpriteAnchor {
    private SpriteAnchor() {}

    /**
     * Feet bottom-center at {@code (worldX, worldY)} in cells; sprite extends upward.
     *
     * @return draw origin {@code [px, py]} for {@link com.badlogic.gdx.graphics.g2d.SpriteBatch#draw}
     */
    public static float[] feetBottomCenter(float worldX, float worldY, float spriteWidthPx, float spriteHeightPx) {
        float feetPxX = worldX * Constants.CELL_SIZE_PX;
        float feetPxY = worldY * Constants.CELL_SIZE_PX;
        float px = feetPxX - spriteWidthPx / 2f;
        float py = feetPxY;
        return new float[] {px, py};
    }

    /** Block sprite: bottom-left of occupancy cell. */
    public static float[] cellBottomLeft(int cellX, int cellY, float spriteWidthPx, float spriteHeightPx) {
        float px = cellX * Constants.CELL_SIZE_PX;
        float py = cellY * Constants.CELL_SIZE_PX;
        return new float[] {px, py};
    }

    /** Block sprite: bottom edge on cell floor, centered horizontally on the cell. */
    public static float[] cellBottomCenter(int cellX, int cellY, float spriteWidthPx, float spriteHeightPx) {
        float cellPx = cellX * Constants.CELL_SIZE_PX;
        float baseY = cellY * Constants.CELL_SIZE_PX;
        float px = cellPx + Constants.CELL_SIZE_PX / 2f - spriteWidthPx / 2f;
        return new float[] {px, baseY};
    }

    /** Block sprite: centered on the occupancy cell (equal bleed on all sides when larger than the cell). */
    public static float[] cellCenter(int cellX, int cellY, float spriteWidthPx, float spriteHeightPx) {
        float cellPx = cellX * Constants.CELL_SIZE_PX;
        float cellPy = cellY * Constants.CELL_SIZE_PX;
        float halfCell = Constants.CELL_SIZE_PX / 2f;
        float px = cellPx + halfCell - spriteWidthPx / 2f;
        float py = cellPy + halfCell - spriteHeightPx / 2f;
        return new float[] {px, py};
    }
}
