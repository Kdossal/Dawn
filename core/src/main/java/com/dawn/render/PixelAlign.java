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
        float snapped = Math.round(cameraLogicalPx * Constants.DISPLAY_SCALE) / (float) Constants.DISPLAY_SCALE;
        return snapped - cameraLogicalPx;
    }
}
