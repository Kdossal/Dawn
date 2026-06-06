package com.dawn.render;

/** Runtime toggles for world drawing. */
public final class RenderSettings {
    /** When false, all blocks draw at full opacity regardless of per-block fade flags. */
    public boolean occlusionFadeEnabled = true;

    public RenderSettings() {}
}
