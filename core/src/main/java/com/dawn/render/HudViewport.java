package com.dawn.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dawn.config.Constants;

/** Window-space viewport for hotbar and debug HUD ({@link Constants#HUD_WIDTH_PX}×{@link Constants#HUD_HEIGHT_PX}). */
public final class HudViewport {
    private final FitViewport viewport = new FitViewport(Constants.HUD_WIDTH_PX, Constants.HUD_HEIGHT_PX);

    public FitViewport viewport() {
        return viewport;
    }

    public void update(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, true);
    }

    public void apply(OrthographicCamera hudCamera) {
        viewport.setCamera(hudCamera);
        viewport.apply();
    }

    /** Screen coords (LibGDX origin bottom-left) → HUD layout coords. */
    public void unproject(float screenX, float screenY, Vector2 out) {
        out.set(screenX, screenY);
        viewport.unproject(out);
    }
}
