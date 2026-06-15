package com.dawn.render;

/** Runtime toggles for world drawing. */
public final class RenderSettings {
    /** When false, all blocks draw at full opacity regardless of per-block fade flags. */
    public boolean occlusionFadeEnabled = true;

    /** When false, skip time-of-day ambient mood and strength on tiles (block light still applies). */
    public boolean dayNightEnabled = true;

    /** When false, skip per-tile block-light sprite tinting. */
    public boolean localLightingEnabled = true;

    /** When true, apply {@link #displayGamma} to final tile tints (display comfort only). */
    public boolean displayGammaEnabled = false;

    /** Shadow lift when gamma enabled; values below 1 brighten low channels. */
    public float displayGamma = 0.85f;

    public RenderSettings() {}
}
