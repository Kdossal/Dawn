package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import org.junit.jupiter.api.Test;

class HudSlotDesignTest {
    @Test
    void scaledDimensionsUseHudArtMult() {
        assertEquals(60f, HudSlotDesign.slotPx(), 0.001f);
        assertEquals(48f, HudSlotDesign.iconPx(), 0.001f);
        assertEquals(6f, HudSlotDesign.gapPx(), 0.001f);
        assertEquals(7.5f, HudSlotDesign.countPadPx(), 0.001f);
        assertEquals(12f, HudSlotDesign.bottomMarginPx(), 0.001f);
        assertEquals(Constants.HUD_ART_MULT, HudSlotDesign.artMult());
    }

    @Test
    void barCenteredHorizontally() {
        float expectedOrigin = (Constants.HUD_WIDTH_PX - HudSlotDesign.barWidth()) / 2f;
        assertEquals(expectedOrigin, HudSlotDesign.barOriginX(), 0.001f);
    }

    @Test
    void slotBoundsMatchHotbarRow() {
        Rectangle slot = new Rectangle();
        HudSlotDesign.slotBounds(0, slot);
        assertEquals(HudSlotDesign.barOriginX(), slot.x, 0.001f);
        assertEquals(HudSlotDesign.barOriginY(), slot.y, 0.001f);
        assertEquals(HudSlotDesign.slotPx(), slot.width, 0.001f);
        assertEquals(HudSlotDesign.slotPx(), slot.height, 0.001f);

        HudSlotDesign.slotBounds(1, slot);
        assertEquals(HudSlotDesign.slotX(1), slot.x, 0.001f);
    }

    @Test
    void iconRatioMatchesSlotUi() {
        assertEquals(
                HudSlotDesign.iconPx(),
                SlotUi.iconPxForSlot(HudSlotDesign.slotPx()),
                0.001f);
    }
}
