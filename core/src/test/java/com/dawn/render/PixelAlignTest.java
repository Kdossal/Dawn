package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.config.Constants;
import org.junit.jupiter.api.Test;

class PixelAlignTest {
    @Test
    void gridOffset_snapsToDisplayScaleSteps() {
        float camera = 100.3f;
        float offset = PixelAlign.gridOffset(camera);
        float aligned = camera + offset;
        assertEquals(
                Math.round(camera * Constants.DISPLAY_SCALE) / (float) Constants.DISPLAY_SCALE,
                aligned,
                0.0001f);
    }

    @Test
    void gridOffset_zeroWhenAlreadyAligned() {
        assertEquals(0f, PixelAlign.gridOffset(16f), 0.0001f);
        assertEquals(0f, PixelAlign.gridOffset(16.5f), 0.0001f);
    }
}
