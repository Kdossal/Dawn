package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LightColorTest {
    @Test
    void normalizeChroma_scalesMaxToOne() {
        float[] c = LightColor.normalizeChroma(0.5f, 0.25f, 1f);
        assertEquals(0.5f, c[0], 0.001f);
        assertEquals(0.25f, c[1], 0.001f);
        assertEquals(1f, c[2], 0.001f);
    }

    @Test
    void normalizeChroma_zeroInput_isWhite() {
        float[] c = LightColor.normalizeChroma(0f, 0f, 0f);
        assertEquals(1f, c[0], 0.001f);
        assertEquals(1f, c[1], 0.001f);
        assertEquals(1f, c[2], 0.001f);
    }

    @Test
    void blendChroma_weightZero_returnsA() {
        float[] a = {1f, 0.5f, 0.2f};
        float[] b = {0.2f, 0.5f, 1f};
        float[] out = LightColor.blendChroma(a, b, 0f);
        assertEquals(1f, out[0], 0.001f);
        assertEquals(0.5f, out[1], 0.001f);
        assertEquals(0.2f, out[2], 0.001f);
    }

    @Test
    void blendChroma_weightOne_returnsB() {
        float[] a = {1f, 0.5f, 0.2f};
        float[] b = {0.2f, 0.5f, 1f};
        float[] out = LightColor.blendChroma(a, b, 1f);
        assertEquals(0.2f, out[0], 0.001f);
        assertEquals(0.5f, out[1], 0.001f);
        assertEquals(1f, out[2], 0.001f);
    }

    @Test
    void scale_multipliesLevelByChroma() {
        float[] out = LightColor.scale(0.5f, new float[] {1f, 0.8f, 0.4f});
        assertEquals(0.5f, out[0], 0.001f);
        assertEquals(0.4f, out[1], 0.001f);
        assertEquals(0.2f, out[2], 0.001f);
    }

    @Test
    void applyMinLightLevel_preservesHueWhenLifting() {
        float[] out = LightColor.applyMinLightLevel(new float[] {0.02f, 0.04f, 0.1f}, 0.12f);
        assertEquals(0.12f, LightColor.maxChannel(out), 0.001f);
        assertTrue(out[2] > out[0], "blue should remain dominant after floor lift");
    }

    @Test
    void applyMinLightLevel_noOpWhenAboveFloor() {
        float[] rgb = {0.5f, 0.4f, 0.3f};
        float[] out = LightColor.applyMinLightLevel(rgb, 0.12f);
        assertEquals(rgb[0], out[0], 0.001f);
        assertEquals(rgb[1], out[1], 0.001f);
        assertEquals(rgb[2], out[2], 0.001f);
    }

    @Test
    void applyDisplayGamma_disabled_passesThrough() {
        float[] rgb = {0.1f, 0.2f, 0.3f};
        float[] out = LightColor.applyDisplayGamma(rgb, false, 0.85f);
        assertEquals(0.1f, out[0], 0.001f);
        assertEquals(0.2f, out[1], 0.001f);
        assertEquals(0.3f, out[2], 0.001f);
    }

    @Test
    void applyDisplayGamma_enabled_liftsLowValues() {
        float[] out = LightColor.applyDisplayGamma(new float[] {0.1f, 0.1f, 0.1f}, true, 0.5f);
        assertTrue(out[0] > 0.1f, "gamma should lift shadow values");
    }

    @Test
    void raiseChannelsToBaseline_clampsSaturatedTintToAmbient() {
        float[] baseline = {1f, 1f, 1f};
        float[] lit = {0.1f, 0.1f, 1f};
        float[] out = LightColor.raiseChannelsToBaseline(lit, baseline);
        assertEquals(1f, out[0], 0.001f);
        assertEquals(1f, out[1], 0.001f);
        assertEquals(1f, out[2], 0.001f);
    }

    @Test
    void raiseChannelsToBaseline_preservesStrongerBlockLightAtNight() {
        float[] baseline = {0.07f, 0.08f, 0.12f};
        float[] lit = {0.05f, 0.06f, 0.95f};
        float[] out = LightColor.raiseChannelsToBaseline(lit, baseline);
        assertEquals(0.07f, out[0], 0.001f);
        assertEquals(0.08f, out[1], 0.001f);
        assertEquals(0.95f, out[2], 0.001f);
    }
}
