package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.DayNightConfig;
import com.dawn.config.GameConfig;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import com.dawn.world.light.LightEngine;
import org.junit.jupiter.api.Test;

class TileLightingTest {
    private static final boolean NO_GAMMA = false;
    private static final float GAMMA = 0.85f;

    @Test
    void nightWithoutBlockLight_respectsMinLightLevelFloor() {
        World world = TestWorlds.smallWalkable(8, 8);
        float nightTime = 0.9f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());

        TileLighting.TileLight light =
                TileLighting.sample(world, 4, 4, nightTime, config, true, true, NO_GAMMA, GAMMA);

        assertEquals(config.minLightLevel, LightColor.maxChannel(new float[] {light.r(), light.g(), light.b()}), 0.001f);
        assertTrue(light.b() > light.r(), "unlit night should be cool tinted");
    }

    @Test
    void day_isFullBrightness() {
        World world = TestWorlds.smallWalkable(8, 8);
        float dayTime = 0.35f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());

        float brightness =
                TileLighting.brightness(world, 4, 4, dayTime, config, true, true, NO_GAMMA, GAMMA);
        assertEquals(1.0f, brightness, 0.001f);
    }

    @Test
    void nightWithLantern_isBrighterThanAmbientFloor() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float nightTime = 0.9f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());
        float brightness =
                TileLighting.brightness(world, 8, 9, nightTime, config, true, true, NO_GAMMA, GAMMA);

        assertTrue(brightness > config.minLightLevel);
    }

    @Test
    void sunsetMidpoint_brightnessIsBetweenNightAndDay() {
        World world = TestWorlds.smallWalkable(8, 8);
        DayNightConfig config = DayNightConfig.from(GameConfig.get());

        float sunsetMid = config.sunsetStart() + (config.sunsetFraction() / 2f);
        float brightness =
                TileLighting.brightness(world, 4, 4, sunsetMid, config, true, true, NO_GAMMA, GAMMA);

        assertTrue(brightness > config.minLightLevel, "sunset midpoint should be brighter than night floor");
        assertTrue(brightness < 1.0f, "sunset midpoint should be darker than full day");
    }

    @Test
    void sunriseMidpoint_brightnessIsBetweenNightAndDay() {
        World world = TestWorlds.smallWalkable(8, 8);
        DayNightConfig config = DayNightConfig.from(GameConfig.get());

        float sunriseMid = config.sunriseStart() + (config.sunriseFraction() / 2f);
        float brightness =
                TileLighting.brightness(world, 4, 4, sunriseMid, config, true, true, NO_GAMMA, GAMMA);

        assertTrue(brightness > config.minLightLevel, "sunrise midpoint should be brighter than night floor");
        assertTrue(brightness < 1.0f, "sunrise midpoint should be darker than full day");
    }

    @Test
    void disabledLocalLighting_returnsFullWhite() {
        World world = TestWorlds.smallWalkable(8, 8);
        world.setObject(4, 4, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float brightness =
                TileLighting.brightness(
                        world, 4, 4, 0.9f, DayNightConfig.from(GameConfig.get()), false, true, NO_GAMMA, GAMMA);
        assertEquals(1f, brightness);
    }

    @Test
    void disabledDayNight_usesNeutralAmbientButKeepsBlockLight() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float nightTime = 0.9f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());
        TileLighting.TileLight light =
                TileLighting.sample(world, 8, 9, nightTime, config, true, false, NO_GAMMA, GAMMA);

        assertTrue(light.r() > config.minLightLevel, "block light should still apply when day/night is off");
    }

    @Test
    void emissiveLantern_atNight_usesEmissionLevel() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float nightTime = 0.9f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());
        TileLighting.TileLight light =
                TileLighting.sample(world, 8, 8, nightTime, config, true, true, NO_GAMMA, GAMMA);

        float emission = BlockDefinitions.lightEmission(BlockId.LANTERN);
        assertTrue(light.r() >= light.g(), "emissive lantern should read warm at night");
        assertEquals(
                emission,
                LightColor.maxChannel(new float[] {light.r(), light.g(), light.b()}),
                0.02f,
                "lantern body brightness should follow lightEmission, not ambient");
    }

    @Test
    void emissiveLantern_atSunrise_staysWarmNotNeutralWhite() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        DayNightConfig config = DayNightConfig.from(GameConfig.get());
        float sunriseMid = config.sunriseStart() + (config.sunriseFraction() * 0.85f);
        TileLighting.TileLight source =
                TileLighting.sample(world, 8, 8, sunriseMid, config, true, true, NO_GAMMA, GAMMA);
        TileLighting.TileLight ring =
                TileLighting.sample(world, 9, 8, sunriseMid, config, true, true, NO_GAMMA, GAMMA);

        assertTrue(source.r() >= source.g(), "lantern source should stay warm during orange sunrise");
        assertTrue(ring.r() >= ring.g() * 0.85f, "lantern ring should stay warm during sunrise");
    }

    @Test
    void dayWithLantern_ringIsFullBrightness() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float dayTime = 0.35f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());
        float brightness =
                TileLighting.brightness(world, 9, 8, dayTime, config, true, true, NO_GAMMA, GAMMA);

        assertEquals(1f, brightness, 0.001f);
    }

    @Test
    void emissiveLantern_atDay_usesWarmChroma() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float dayTime = 0.35f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());
        TileLighting.TileLight light =
                TileLighting.sample(world, 8, 8, dayTime, config, true, true, NO_GAMMA, GAMMA);

        assertTrue(light.r() >= light.g(), "emissive lantern keeps warm chroma at day");
        assertEquals(1f, light.r(), 0.001f);
    }

    @Test
    void saturatedBlockLight_atDay_doesNotDarkenBelowAmbient() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float dayTime = 0.35f;
        DayNightConfig config = DayNightConfig.from(GameConfig.get());
        TileLighting.TileLight ring =
                TileLighting.sample(world, 9, 8, dayTime, config, true, true, NO_GAMMA, GAMMA);

        assertEquals(1f, ring.r(), 0.001f);
        assertEquals(1f, ring.g(), 0.001f);
        assertEquals(1f, ring.b(), 0.001f);
    }

    @Test
    void displayGamma_liftsVeryDarkTiles() {
        World world = TestWorlds.smallWalkable(8, 8);
        float nightTime = 0.9f;
        DayNightConfig config = new DayNightConfig();
        config.minLightLevel = 0.05f;

        TileLighting.TileLight without =
                TileLighting.sample(world, 4, 4, nightTime, config, true, true, false, 0.5f);
        TileLighting.TileLight with =
                TileLighting.sample(world, 4, 4, nightTime, config, true, true, true, 0.5f);

        float withoutMax = LightColor.maxChannel(new float[] {without.r(), without.g(), without.b()});
        float withMax = LightColor.maxChannel(new float[] {with.r(), with.g(), with.b()});
        assertTrue(withMax > withoutMax, "enabled gamma should lift shadow values");
    }
}
