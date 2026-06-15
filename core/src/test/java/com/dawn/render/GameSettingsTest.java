package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.config.Constants;
import org.junit.jupiter.api.Test;

class GameSettingsTest {
    @Test
    void level1IsMaxZoomOut() {
        GameSettings settings = new GameSettings();
        settings.setZoomLevel(1);

        assertEquals(1f, settings.targetZoomFactor(), 0.0001f);
        assertEquals(Constants.VIEW_WIDTH_PX, GameSettings.viewWidthPx(settings.targetZoomFactor()), 0.01f);
        assertEquals(Constants.VIEW_HEIGHT_PX, GameSettings.viewHeightPx(settings.targetZoomFactor()), 0.01f);
    }

    @Test
    void level10IsMaxZoomIn() {
        float factor = GameSettings.zoomFactorForLevel(10);

        assertEquals(GameSettings.MAX_ZOOM_IN_FACTOR, factor, 0.001f);
        assertEquals(384f, GameSettings.viewWidthPx(factor), 0.5f);
        assertEquals(240f, GameSettings.viewHeightPx(factor), 0.5f);
    }

    @Test
    void level5InterpolatesLinearly() {
        float factor = GameSettings.zoomFactorForLevel(5);
        float expected = 1f + (GameSettings.MAX_ZOOM_IN_FACTOR - 1f) * (4f / 9f);

        assertEquals(expected, factor, 0.001f);
    }

    @Test
    void clampZoomLevel() {
        GameSettings settings = new GameSettings();
        settings.setZoomLevel(0);
        assertEquals(1, settings.zoomLevel);
        settings.setZoomLevel(99);
        assertEquals(10, settings.zoomLevel);
    }

    @Test
    void defaultUiSizeIsMedium() {
        GameSettings settings = new GameSettings();
        assertEquals(GameSettings.UiSize.MEDIUM, settings.uiSize);
    }

    @Test
    void slotMultipliersAreIntegerSteps() {
        assertEquals(2, GameSettings.slotMultiplier(GameSettings.UiSize.SMALL));
        assertEquals(3, GameSettings.slotMultiplier(GameSettings.UiSize.MEDIUM));
        assertEquals(4, GameSettings.slotMultiplier(GameSettings.UiSize.LARGE));
    }

    @Test
    void cycleUiSizeOrder() {
        GameSettings settings = new GameSettings();
        settings.setUiSize(GameSettings.UiSize.SMALL);
        settings.cycleUiSize();
        assertEquals(GameSettings.UiSize.MEDIUM, settings.uiSize);
        settings.cycleUiSize();
        assertEquals(GameSettings.UiSize.LARGE, settings.uiSize);
        settings.cycleUiSize();
        assertEquals(GameSettings.UiSize.SMALL, settings.uiSize);
    }

    @Test
    void uiSizeLabels() {
        assertEquals("Small", GameSettings.uiSizeLabel(GameSettings.UiSize.SMALL));
        assertEquals("Medium", GameSettings.uiSizeLabel(GameSettings.UiSize.MEDIUM));
        assertEquals("Large", GameSettings.uiSizeLabel(GameSettings.UiSize.LARGE));
    }

    @Test
    void displayGammaDefaultIsDark() {
        GameSettings settings = new GameSettings();
        assertEquals(-1, settings.displayGammaPreset);
        assertEquals("Dark", GameSettings.displayGammaLabel(settings.displayGammaPreset));
    }

    @Test
    void cycleDisplayGammaPresetOrder() {
        GameSettings settings = new GameSettings();
        settings.cycleDisplayGammaPreset();
        assertEquals(0, settings.displayGammaPreset);
        assertEquals("Normal", GameSettings.displayGammaLabel(settings.displayGammaPreset));
        settings.cycleDisplayGammaPreset();
        assertEquals(1, settings.displayGammaPreset);
        assertEquals("Bright", GameSettings.displayGammaLabel(settings.displayGammaPreset));
        settings.cycleDisplayGammaPreset();
        assertEquals(2, settings.displayGammaPreset);
        assertEquals("Brighter", GameSettings.displayGammaLabel(settings.displayGammaPreset));
        settings.cycleDisplayGammaPreset();
        assertEquals(-1, settings.displayGammaPreset);
    }

    @Test
    void applyDisplayGamma_updatesRenderSettings() {
        GameSettings settings = new GameSettings();
        RenderSettings render = new RenderSettings();
        settings.displayGammaPreset = 0;
        settings.applyDisplayGamma(render);
        assertEquals(true, render.displayGammaEnabled);
        assertEquals(0.92f, render.displayGamma, 0.001f);
        settings.displayGammaPreset = -1;
        settings.applyDisplayGamma(render);
        assertEquals(false, render.displayGammaEnabled);
    }
}
