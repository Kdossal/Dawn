package com.dawn.render;

import com.dawn.config.Constants;

/** Player-adjustable game settings (in-memory for now). */
public final class GameSettings {
    public static final int MIN_ZOOM_LEVEL = 1;
    public static final int MAX_ZOOM_LEVEL = 10;
    public static final int DEFAULT_ZOOM_LEVEL = 5;
    /** Max zoom in: 40% closer than max zoom out (visible area × 0.6). */
    public static final float MAX_ZOOM_IN_FACTOR = 1f / 0.6f;

    public int zoomLevel = DEFAULT_ZOOM_LEVEL;

    public void setZoomLevel(int level) {
        zoomLevel = clampZoomLevel(level);
    }

    public void adjustZoomLevel(int delta) {
        setZoomLevel(zoomLevel + delta);
    }

    public float targetZoomFactor() {
        return zoomFactorForLevel(zoomLevel);
    }

    public static float zoomFactorForLevel(int level) {
        int clamped = clampZoomLevel(level);
        float t = (clamped - MIN_ZOOM_LEVEL) / (float) (MAX_ZOOM_LEVEL - MIN_ZOOM_LEVEL);
        return lerp(1f, MAX_ZOOM_IN_FACTOR, t);
    }

    public static float viewWidthPx(float zoomFactor) {
        return Constants.VIEW_WIDTH_PX / zoomFactor;
    }

    public static float viewHeightPx(float zoomFactor) {
        return Constants.VIEW_HEIGHT_PX / zoomFactor;
    }

    private static int clampZoomLevel(int level) {
        return Math.max(MIN_ZOOM_LEVEL, Math.min(MAX_ZOOM_LEVEL, level));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
