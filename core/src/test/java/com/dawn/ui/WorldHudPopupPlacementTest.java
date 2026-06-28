package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.config.Constants;
import com.dawn.render.GameSettings;
import org.junit.jupiter.api.Test;

/** Verifies camera-relative popup math against known min-zoom reference points. */
class WorldHudPopupPlacementTest {
    private static final float EPS = 0.5f;

    @Test
    void centeredPlayer_minZoom_spriteTopAndPanelMatchReference() {
        float zoomFactor = GameSettings.zoomFactorForLevel(GameSettings.MIN_ZOOM_LEVEL);
        float viewH = GameSettings.viewHeightPx(zoomFactor);
        float scaleY = Constants.HUD_HEIGHT_PX / viewH;

        float spriteTopStageY = Constants.HUD_HEIGHT_PX * 0.5f + Constants.PLAYER_SPRITE_HEIGHT_PX * scaleY;
        assertEquals(744f, spriteTopStageY, EPS);
        assertEquals(456f, WorldHudPopupPlacement.stageYToTopLeftY(spriteTopStageY), 2f);

        WorldHudPopupPlacement.Config config = WorldHudPopupPlacement.Config.crateStorage();
        WorldHudPopupPlacement.PanelPosition panel =
                WorldHudPopupPlacement.panelAboveAnchor(
                        new WorldHudPopupPlacement.Anchor(Constants.HUD_WIDTH_PX * 0.5f, spriteTopStageY),
                        180f,
                        config);

        assertEquals(spriteTopStageY + config.gapAboveAnchorPx(), panel.panelBottomY(), EPS);
        assertEquals(441f, WorldHudPopupPlacement.stageYToTopLeftY(panel.panelBottomY()), 2f);
    }
}
