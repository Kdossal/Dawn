package com.dawn.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

/** Smooth world zoom driven by {@link GameSettings#zoomLevel}. */
public final class ZoomController {
    private static final float LERP_SPEED = 10f;

    private final GameSettings settings;
    private float displayedZoomFactor = 1f;

    public ZoomController(GameSettings settings) {
        this.settings = settings;
        displayedZoomFactor = settings.targetZoomFactor();
    }

    public float displayedZoomFactor() {
        return displayedZoomFactor;
    }

    public float viewWidthPx() {
        return GameSettings.viewWidthPx(displayedZoomFactor);
    }

    public float viewHeightPx() {
        return GameSettings.viewHeightPx(displayedZoomFactor);
    }

    public void update(float delta) {
        float target = settings.targetZoomFactor();
        if (MathUtils.isEqual(displayedZoomFactor, target, 0.0001f)) {
            displayedZoomFactor = target;
            return;
        }
        float alpha = Math.min(1f, LERP_SPEED * delta);
        displayedZoomFactor = MathUtils.lerp(displayedZoomFactor, target, alpha);
    }

    public void applyTo(OrthographicCamera worldCamera, GameViewport gameViewport) {
        float w = viewWidthPx();
        float h = viewHeightPx();
        worldCamera.setToOrtho(false, w, h);
        gameViewport.setWorldSize(w, h);
    }
}
