package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.DawnTypography.TextTier;
import org.junit.jupiter.api.Test;

class DawnTypographyTest {
    @Test
    void hudScalesMatchTierScreenPxOverBaseLine() {
        assertEquals(1f, DawnTypography.scale(TextTier.XS, TextContext.HUD));
        assertEquals(2f, DawnTypography.scale(TextTier.SM, TextContext.HUD));
        assertEquals(3f, DawnTypography.scale(TextTier.MD, TextContext.HUD));
        assertEquals(4f, DawnTypography.scale(TextTier.LG, TextContext.HUD));
        assertEquals(6f, DawnTypography.scale(TextTier.XL, TextContext.HUD));
    }

    @Test
    void slotCountHudScaleMatchesSmHud() {
        assertEquals(DawnTypography.scale(TextTier.SM, TextContext.HUD), DawnTypography.slotCountHudScale());
    }

    @Test
    void inventoryDesignScalesAccountForUiScale() {
        assertEquals(0.2f, DawnTypography.scale(TextTier.XS, TextContext.INVENTORY_DESIGN));
        assertEquals(0.4f, DawnTypography.scale(TextTier.SM, TextContext.INVENTORY_DESIGN));
        assertEquals(0.6f, DawnTypography.scale(TextTier.MD, TextContext.INVENTORY_DESIGN));
    }
}
