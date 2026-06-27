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

    /** -1 = off; 0..2 index into {@link #DISPLAY_GAMMA_VALUES}. */
    public int displayGammaPreset = -1;

    public static final float[] DISPLAY_GAMMA_VALUES = {0.92f, 0.85f, 0.75f};

    /** Off → Low → Normal → High → Off. */
    public void cycleDisplayGammaPreset() {
        displayGammaPreset++;
        if (displayGammaPreset >= DISPLAY_GAMMA_VALUES.length) {
            displayGammaPreset = -1;
        }
    }

    public static String displayGammaLabel(int preset) {
        return switch (preset) {
            case 0 -> "Normal";
            case 1 -> "Bright";
            case 2 -> "Brighter";
            default -> "Dark";
        };
    }

    /** Copies shadow-lift preference into runtime render toggles. */
    public void applyDisplayGamma(RenderSettings renderSettings) {
        if (displayGammaPreset < 0) {
            renderSettings.displayGammaEnabled = false;
            return;
        }
        renderSettings.displayGammaEnabled = true;
        renderSettings.displayGamma = DISPLAY_GAMMA_VALUES[displayGammaPreset];
    }

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
