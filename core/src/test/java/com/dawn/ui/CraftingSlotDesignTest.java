package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.config.Constants;
import org.junit.jupiter.api.Test;

class CraftingSlotDesignTest {
    @Test
    void chromeSheetIsEightyPxWideAtArtBase() {
        assertEquals(80, CraftingSlotDesign.BASE_CHROME_W_PX);
        assertEquals(80f, CraftingSlotDesign.chromeWx() / Constants.HUD_ART_MULT, 0.001f);
    }
}
