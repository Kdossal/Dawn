package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.ui.DawnTypography.TextTier;
import org.junit.jupiter.api.Test;

class DawnFontsTest {
    @Test
    void nativePointSizeMatchesXsTier() {
        assertEquals(TextTier.XS.screenPx(), DawnFonts.NATIVE_POINT_SIZE);
    }
}
