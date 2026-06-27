package com.dawn.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.dawn.config.Constants;
import com.dawn.render.SpriteAnchor;

/**
 * Positions in-world HUD popups above an entity sprite in HUD stage coordinates.
 *
 * <p>Maps world offset from the camera center into {@link Constants#HUD_WIDTH_PX}×
 * {@link Constants#HUD_HEIGHT_PX} space. {@link Config#gapAboveAnchorBasePx()} is multiplied by
 * {@link Constants#HUD_ART_MULT}.
 */
public final class WorldHudPopupPlacement {
    private WorldHudPopupPlacement() {}

    /** Tunable placement settings for a popup. */
    public record Config(float gapAboveAnchorBasePx) {
        public static Config crateStorage() {
            return new Config(5f);
        }

        public static Config crafting() {
            return new Config(5f);
        }

        public float gapAboveAnchorPx() {
            return gapAboveAnchorBasePx * Constants.HUD_ART_MULT;
        }
    }

    /** Sprite-top anchor in HUD stage space (X = horizontal center of sprite). */
    public record Anchor(float stageX, float stageY) {}

    /** Bottom-left position for a popup panel in stage space. */
    public record PanelPosition(float panelX, float panelBottomY) {}

    /**
     * Resolves the sprite-top anchor for an entity drawn with {@link SpriteAnchor#feetBottomCenter}.
     */
    public static Anchor spriteTopCenter(
            float feetCellX,
            float feetCellY,
            float spriteWidthPx,
            float spriteHeightPx,
            OrthographicCamera worldCamera) {
        float[] origin = SpriteAnchor.feetBottomCenter(feetCellX, feetCellY, spriteWidthPx, spriteHeightPx);
        float spriteTopWorldPx = origin[1] + spriteHeightPx;
        float spriteCenterWorldPx = origin[0] + spriteWidthPx * 0.5f;
        return worldPointToStageAnchor(spriteCenterWorldPx, spriteTopWorldPx, worldCamera);
    }

    /**
     * Maps a world logical-pixel point to HUD stage coordinates via camera-relative scaling.
     */
    public static Anchor worldPointToStageAnchor(
            float worldPxX, float worldPxY, OrthographicCamera worldCamera) {
        return worldDeltaToStageAnchor(
                worldPxX - worldCamera.position.x,
                worldPxY - worldCamera.position.y,
                worldCamera);
    }

    /**
     * Maps world offset from camera center to HUD stage coordinates.
     */
    public static Anchor worldDeltaToStageAnchor(
            float deltaWorldX, float deltaWorldY, OrthographicCamera worldCamera) {
        float scaleX = Constants.HUD_WIDTH_PX / worldCamera.viewportWidth;
        float scaleY = Constants.HUD_HEIGHT_PX / worldCamera.viewportHeight;
        float hudCenterX = Constants.HUD_WIDTH_PX * 0.5f;
        float hudCenterY = Constants.HUD_HEIGHT_PX * 0.5f;
        return new Anchor(hudCenterX + deltaWorldX * scaleX, hudCenterY + deltaWorldY * scaleY);
    }

    /** Panel bottom-left so the bottom edge sits {@code gapAboveAnchorPx} above the anchor. */
    public static PanelPosition panelAboveAnchor(Anchor anchor, float panelWidth, Config config) {
        float gap = config.gapAboveAnchorPx();
        return new PanelPosition(anchor.stageX() - panelWidth * 0.5f, anchor.stageY() + gap);
    }

    /** Clamps panel horizontally; only prevents falling below stage bottom. */
    public static PanelPosition clampToStage(PanelPosition pos, float panelWidth, float panelHeight) {
        float hudW = Constants.HUD_WIDTH_PX;
        float x = Math.max(0f, Math.min(pos.panelX(), hudW - panelWidth));
        float y = Math.max(0f, pos.panelBottomY());
        return new PanelPosition(x, y);
    }

    /** Convenience: top-left screen Y (Y-down) for debugging — {@code hudHeight - stageY}. */
    public static float stageYToTopLeftY(float stageY) {
        return Constants.HUD_HEIGHT_PX - stageY;
    }
}
