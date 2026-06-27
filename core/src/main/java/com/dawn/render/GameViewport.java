package com.dawn.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
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

    /** Logical world coords → screen coords. {@link #apply} must have been called for the same camera. */
    public void project(Vector3 logicalToScreen) {
        logicalToScreen.z = 0f;
        viewport.project(logicalToScreen);
    }

    /**
     * World offset from camera center → screen pixels (LibGDX bottom-left origin).
     * Camera center maps to the game viewport screen center.
     */
    public void worldDeltaToScreen(
            float deltaWorldX, float deltaWorldY, OrthographicCamera camera, Vector2 out) {
        float scaleX = viewport.getScreenWidth() / camera.viewportWidth;
        float scaleY = viewport.getScreenHeight() / camera.viewportHeight;
        float screenCenterX = viewport.getScreenX() + viewport.getScreenWidth() * 0.5f;
        float screenCenterY = viewport.getScreenY() + viewport.getScreenHeight() * 0.5f;
        out.x = screenCenterX + deltaWorldX * scaleX;
        out.y = screenCenterY + deltaWorldY * scaleY;
    }

    /**
     * World logical pixels → screen pixels (LibGDX bottom-left origin), accounting for FitViewport
     * letterboxing. {@code viewport.update} must have run for the current window size.
     */
    public void worldToScreen(float worldX, float worldY, OrthographicCamera camera, Vector2 out) {
        worldDeltaToScreen(worldX - camera.position.x, worldY - camera.position.y, camera, out);
    }

    /**
     * World logical pixels → stage coordinates for a {@link com.badlogic.gdx.scenes.scene2d.Stage}
     * using this viewport's screen mapping.
     */
    public void worldPointToStage(
            com.badlogic.gdx.scenes.scene2d.Stage stage,
            float worldX,
            float worldY,
            OrthographicCamera camera,
            Vector2 out) {
        worldToScreen(worldX, worldY, camera, out);
        stage.screenToStageCoordinates(out);
    }

    /** Screen coords → logical world/HUD coords. {@link #apply} must have been called for the same camera. */
    public void unproject(Vector3 screenToLogical) {
        screenToLogical.z = 0f;
        viewport.unproject(screenToLogical);
    }

}
