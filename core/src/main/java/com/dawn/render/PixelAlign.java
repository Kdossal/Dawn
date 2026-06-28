package com.dawn.render;

import com.dawn.config.Constants;

/** Keeps grid-aligned sprites on integer screen pixels without snapping the camera. */
public final class PixelAlign {
    private PixelAlign() {}

    /**
     * Translation to add to grid sprite draw positions so tiles align to the framebuffer after
     * {@link Constants#DISPLAY_SCALE} upscale while the camera follows smoothly.
     */
    public static float gridOffset(float cameraLogicalPx) {
        float snapped = snapLogicalPx(cameraLogicalPx);
        return snapped - cameraLogicalPx;
    }

    /**
     * Snaps a logical-pixel coordinate to {@link Constants#DISPLAY_SCALE} steps so nearest-filtered
     * art lands on whole framebuffer pixels after upscale.
     */
    public static float snapLogicalPx(float logicalPx) {
        float scale = Constants.DISPLAY_SCALE;
        return Math.round(logicalPx * scale) / scale;
    }

    /**
     * Feet-bottom-center draw origin for entity sprites, with optional camera grid alignment.
     *
     * @return {@code [leftPx, bottomPx]} for {@link com.badlogic.gdx.graphics.g2d.SpriteBatch#draw}
     */
    public static float[] feetBottomCenter(
            float feetCellX,
            float feetCellY,
            float spriteWidthPx,
            float spriteHeightPx,
            float alignOffsetX,
            float alignOffsetY,
            EntityPixelSnap snapMode,
            float moveX,
            float moveY) {
        float[] origin = SpriteAnchor.feetBottomCenter(feetCellX, feetCellY, spriteWidthPx, spriteHeightPx);
        float left = origin[0] + alignOffsetX;
        float bottom = origin[1] + alignOffsetY;
        return switch (snapMode) {
            case DISPLAY_GRID -> new float[] {snapLogicalPx(left), snapLogicalPx(bottom)};
            case AXIS_SMOOTH -> new float[] {left, bottom};
            case DIAGONAL -> new float[] {
                snapAxisMonotonic(left, moveX), snapAxisMonotonic(bottom, moveY)
            };
        };
    }

    /** Quantize along one axis without backward jumps while moving on that axis. */
    static float snapAxisMonotonic(float px, float moveComponent) {
        float scale = Constants.DISPLAY_SCALE;
        if (moveComponent > 1e-4f) {
            return (float) Math.floor(px * scale) / scale;
        }
        if (moveComponent < -1e-4f) {
            return (float) Math.ceil(px * scale) / scale;
        }
        return snapLogicalPx(px);
    }

    /** @see #feetBottomCenter(float, float, float, float, float, float, EntityPixelSnap, float, float) */
    public static float[] snappedFeetBottomCenter(
            float feetCellX,
            float feetCellY,
            float spriteWidthPx,
            float spriteHeightPx,
            float alignOffsetX,
            float alignOffsetY) {
        return feetBottomCenter(
                feetCellX,
                feetCellY,
                spriteWidthPx,
                spriteHeightPx,
                alignOffsetX,
                alignOffsetY,
                EntityPixelSnap.DISPLAY_GRID,
                0f,
                0f);
    }
}
