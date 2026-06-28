package com.dawn.render;

/** How entity sprite draw origins are quantized for pixel-crisp rendering. */
public enum EntityPixelSnap {
    /** Idle / interact: snap both axes to the display-scale grid. */
    DISPLAY_GRID,
    /** Cardinal walk: camera align only, no quantize. */
    AXIS_SMOOTH,
    /** Diagonal walk: monotonic floor/ceil per axis to stop 2-axis subpixel shimmer. */
    DIAGONAL;

    private static final float DIAGONAL_MOVE_THRESHOLD = 0.15f;

    public static EntityPixelSnap forMovement(boolean paused, boolean moving, float moveX, float moveY) {
        if (paused || !moving) {
            return DISPLAY_GRID;
        }
        if (Math.abs(moveX) > DIAGONAL_MOVE_THRESHOLD && Math.abs(moveY) > DIAGONAL_MOVE_THRESHOLD) {
            return DIAGONAL;
        }
        return AXIS_SMOOTH;
    }
}
