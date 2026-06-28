package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.DawnTypography.TextTier;
import org.junit.jupiter.api.Test;

class DawnTypographyTest {
    @Test
    void hudUsesNativeTierAtlasesAtUnityScale() {
        assertEquals(1f, DawnTypography.scale(TextTier.XS, TextContext.HUD));
        assertEquals(1f, DawnTypography.scale(TextTier.SM, TextContext.HUD));
        assertEquals(1f, DawnTypography.scale(TextTier.MD, TextContext.HUD));
        assertEquals(1f, DawnTypography.scale(TextTier.LG, TextContext.HUD));
        assertEquals(1f, DawnTypography.scale(TextTier.XL, TextContext.HUD));
    }

    @Test
    void slotCountHudScaleMatchesSmHud() {
        assertEquals(DawnTypography.scale(TextTier.SM, TextContext.HUD), DawnTypography.slotCountHudScale());
    }
}
