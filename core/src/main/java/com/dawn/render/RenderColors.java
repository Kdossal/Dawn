package com.dawn.render;

import com.badlogic.gdx.graphics.Color;

/** Shared tint colors for overlays and HUD (SpriteBatch alpha). */
public final class RenderColors {
    public static final Color TARGET_HIGHLIGHT = new Color(0.55f, 0.82f, 1f, 0.4f);
    public static final Color PLACEMENT_VALID = new Color(0.55f, 0.82f, 1f, 0.4f);
    public static final Color PLACEMENT_INVALID = new Color(0.95f, 0.25f, 0.25f, 0.45f);
    public static final Color REACH_RING = new Color(1f, 1f, 1f, 0.15f);
    public static final Color DROP_LABEL_BG = new Color(0f, 0f, 0f, 0.6f);

    /** Block sprite alpha when the player is behind a fading occluder. */
    public static final float OCCLUSION_FADE_ALPHA = 0.45f;

    /** Full-screen pause overlay tint (light grey). */
    public static final Color PAUSE_DIM_COLOR = new Color(0.55f, 0.55f, 0.55f, 1f);
    public static final float PAUSE_DIM_ALPHA = 0.45f;

    private RenderColors() {}
}
