package com.dawn.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dawn.config.Constants;

/** Maps logical game space ({@value Constants#VIEW_WIDTH_PX}×{@value Constants#VIEW_HEIGHT_PX}) to the window. */
public final class GameViewport {
    private final FitViewport viewport;
    private final Vector3 tmp = new Vector3();

    public GameViewport() {
        viewport = new FitViewport(Constants.VIEW_WIDTH_PX, Constants.VIEW_HEIGHT_PX);
    }

    public void update(int screenWidth, int screenHeight) {
        // Do not center camera on apply — GameScreen.updateCamera() owns position.
        viewport.update(screenWidth, screenHeight, false);
    }

    public void setWorldSize(float worldWidth, float worldHeight) {
        viewport.setWorldSize(worldWidth, worldHeight);
    }

    /** Call before rendering with {@code camera}; required before {@link #unproject}. */
    public void apply(OrthographicCamera camera) {
        viewport.setCamera(camera);
        viewport.apply();
    }

    /** Screen coords → logical world/HUD coords. {@link #apply} must have been called for the same camera. */
    public void unproject(Vector3 screenToLogical) {
        screenToLogical.z = 0f;
        viewport.unproject(screenToLogical);
    }

}
