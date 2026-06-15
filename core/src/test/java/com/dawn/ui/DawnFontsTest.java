package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.render.GameSettings;
import org.junit.jupiter.api.Test;

class DawnFontsTest {
    @Test
    void drawScaleForUiSize() {
        assertEquals(1f, DawnFonts.drawScaleForUiSize(GameSettings.UiSize.SMALL));
        assertEquals(1f, DawnFonts.drawScaleForUiSize(GameSettings.UiSize.MEDIUM));
        assertEquals(2f, DawnFonts.drawScaleForUiSize(GameSettings.UiSize.LARGE));
    }

    @Test
    void mediumPointSizeIsOneAndHalfTimesNative() {
        assertEquals(DawnFonts.NATIVE_POINT_SIZE * 3 / 2, DawnFonts.MEDIUM_POINT_SIZE);
    }
}
