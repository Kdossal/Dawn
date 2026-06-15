package com.dawn.render;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.config.DayNightConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AmbientLightingTest {
    private DayNightConfig config;

    @BeforeEach
    void setUp() {
        config = new DayNightConfig();
    }

    @Test
    void resolvePhase_sunriseStartsAtSixAm() {
        assertEquals(AmbientLighting.Phase.SUNRISE, AmbientLighting.resolvePhase(0.25f, config).phase());
        assertEquals(0f, AmbientLighting.resolvePhase(0.25f, config).phaseT(), 1e-5f);
    }

    @Test
    void resolvePhase_boundaries() {
        float sunriseEnd = config.dayStart();
        float dayEnd = config.sunsetStart();
        float sunsetEnd = config.nightStart();

        assertEquals(AmbientLighting.Phase.SUNRISE, AmbientLighting.resolvePhase(config.sunriseStart(), config).phase());
        assertEquals(AmbientLighting.Phase.DAY, AmbientLighting.resolvePhase(sunriseEnd + 0.001f, config).phase());
        assertEquals(AmbientLighting.Phase.SUNSET, AmbientLighting.resolvePhase(dayEnd + 0.001f, config).phase());
        assertEquals(AmbientLighting.Phase.NIGHT, AmbientLighting.resolvePhase(sunsetEnd + 0.001f, config).phase());
        assertEquals(AmbientLighting.Phase.NIGHT, AmbientLighting.resolvePhase(0.1f, config).phase());
    }

    @Test
    void phaseShares_sumToOne() {
        float sum =
                config.nightFraction()
                        + config.dayFraction()
                        + config.sunriseFraction()
                        + config.sunsetFraction();
        assertEquals(1f, sum, 1e-5f);
        assertEquals(0.46f, config.nightShare, 1e-5f);
        assertEquals(0.46f, config.dayShare, 1e-5f);
    }

    @Test
    void ambientLevel_midDay_isFull() {
        float midDay = (config.dayStart() + config.sunsetStart()) / 2f;
        assertEquals(1f, AmbientLighting.ambientLevel(midDay, config), 0.001f);
    }

    @Test
    void ambientLevel_night_isMinLightLevel() {
        float nightT = config.nightStart() + 0.05f;
        assertEquals(config.minLightLevel, AmbientLighting.ambientLevel(nightT, config), 0.001f);
    }

    @Test
    void ambientChroma_midDay_isWhite() {
        float midDay = (config.dayStart() + config.sunsetStart()) / 2f;
        float[] chroma = AmbientLighting.ambientChroma(midDay, config);
        assertEquals(1f, chroma[0], 0.001f);
        assertEquals(1f, chroma[1], 0.001f);
        assertEquals(1f, chroma[2], 0.001f);
    }

    @Test
    void ambientChroma_night_isBlueDominantAndNormalized() {
        float nightT = config.nightStart() + 0.05f;
        float[] chroma = AmbientLighting.ambientChroma(nightT, config);
        assertTrue(chroma[2] > chroma[0], "night chroma should be blue-dominant");
        assertEquals(1f, LightColor.maxChannel(chroma), 0.001f);
    }

    @Test
    void ambientChroma_sunriseStart_matchesNightChroma() {
        float[] sunrise = AmbientLighting.ambientChroma(config.sunriseStart(), config);
        float[] night = AmbientLighting.ambientChroma(config.nightStart() + 0.05f, config);
        assertEquals(night[0], sunrise[0], 0.02f);
        assertEquals(night[2], sunrise[2], 0.02f);
    }

    @Test
    void ambientChroma_sunriseEnd_isWhite() {
        float[] chroma = AmbientLighting.ambientChroma(config.dayStart() - 0.0001f, config);
        assertEquals(1f, chroma[0], 0.02f);
        assertEquals(1f, chroma[2], 0.02f);
    }

    @Test
    void ambientChroma_sunsetEnd_matchesNightChroma() {
        float[] sunset = AmbientLighting.ambientChroma(config.nightStart() - 0.0001f, config);
        float[] night = AmbientLighting.ambientChroma(config.nightStart() + 0.05f, config);
        assertEquals(night[0], sunset[0], 0.02f);
        assertEquals(night[2], sunset[2], 0.02f);
    }

    @Test
    void sunFactorAt_defaultsToOne() {
        assertEquals(1f, AmbientLighting.sunFactorAt(0, 0, null));
    }
}
