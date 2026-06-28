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
        assertEquals(0f, PixelAlign.gridOffset(50f / 3f), 0.0001f);
    }

    @Test
    void snapLogicalPx_alignsToDisplayScaleSteps() {
        assertEquals(301f / 3f, PixelAlign.snapLogicalPx(100.3f), 0.0001f);
        assertEquals(50f / 3f, PixelAlign.snapLogicalPx(50.1f / 3f), 0.0001f);
    }

    @Test
    void feetBottomCenter_axisSmooth_keepsFractionalOrigin() {
        float[] smooth =
                PixelAlign.feetBottomCenter(
                        4.25f, 8.125f, 32f, 48f, 0.1f, -0.05f, EntityPixelSnap.AXIS_SMOOTH, 1f, 0f);
        assertEquals(4.25f * 16f - 16f + 0.1f, smooth[0], 0.0001f);
    }

    @Test
    void feetBottomCenter_diagonal_snapsMonotonicallyAlongMoveAxes() {
        float leftRaw = 4.25f * 16f - 16f;
        float bottomRaw = 8.125f * 16f;
        float[] origin =
                PixelAlign.feetBottomCenter(
                        4.25f, 8.125f, 32f, 48f, 0f, 0f, EntityPixelSnap.DIAGONAL, 1f, 1f);
        assertEquals(
                Math.floor(leftRaw * Constants.DISPLAY_SCALE) / (float) Constants.DISPLAY_SCALE,
                origin[0],
                0.0001f);
        assertEquals(
                Math.floor(bottomRaw * Constants.DISPLAY_SCALE) / (float) Constants.DISPLAY_SCALE,
                origin[1],
                0.0001f);
    }

    @Test
    void snapAxisMonotonic_quantizesToDisplayScaleSteps() {
        float px = 52.4f;
        assertEquals(
                Math.floor(px * Constants.DISPLAY_SCALE) / (float) Constants.DISPLAY_SCALE,
                PixelAlign.snapAxisMonotonic(px, 1f),
                0.0001f);
        assertEquals(
                Math.ceil(px * Constants.DISPLAY_SCALE) / (float) Constants.DISPLAY_SCALE,
                PixelAlign.snapAxisMonotonic(px, -1f),
                0.0001f);
    }
}
