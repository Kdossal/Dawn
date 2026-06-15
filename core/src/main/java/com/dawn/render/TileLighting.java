package com.dawn.render;

import com.dawn.config.DayNightConfig;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;

/** Combines ambient level/chroma with block light for per-tile sprite tinting. */
public final class TileLighting {
    /** Per-tile RGB tint after level, chroma, floor, and optional display gamma. */
    public record TileLight(float r, float g, float b) {
        public static TileLight fullWhite() {
            return new TileLight(1f, 1f, 1f);
        }

        public static TileLight fromRgb(float[] rgb) {
            return new TileLight(rgb[0], rgb[1], rgb[2]);
        }
    }

    private static final float[] WHITE_CHROMA = {1f, 1f, 1f};

    private TileLighting() {}

    public static TileLight sample(
            World world,
            int cellX,
            int cellY,
            float timeOfDay,
            DayNightConfig config,
            boolean localLightingEnabled,
            boolean dayNightEnabled,
            boolean displayGammaEnabled,
            float displayGamma) {
        if (!localLightingEnabled) {
            return TileLight.fullWhite();
        }
        float minLightLevel = config.minLightLevel;
        float[] rgb =
                computeRgb(
                        world,
                        cellX,
                        cellY,
                        timeOfDay,
                        config,
                        dayNightEnabled,
                        minLightLevel,
                        displayGammaEnabled,
                        displayGamma);
        return TileLight.fromRgb(rgb);
    }

    public static float brightness(
            World world,
            int cellX,
            int cellY,
            float timeOfDay,
            DayNightConfig config,
            boolean localLightingEnabled,
            boolean dayNightEnabled,
            boolean displayGammaEnabled,
            float displayGamma) {
        TileLight light =
                sample(
                        world,
                        cellX,
                        cellY,
                        timeOfDay,
                        config,
                        localLightingEnabled,
                        dayNightEnabled,
                        displayGammaEnabled,
                        displayGamma);
        return LightColor.maxChannel(new float[] {light.r(), light.g(), light.b()});
    }

    public static TileLight[][] buildCache(
            World world,
            int minX,
            int minY,
            int maxX,
            int maxY,
            float timeOfDay,
            DayNightConfig config,
            boolean localLightingEnabled,
            boolean dayNightEnabled,
            boolean displayGammaEnabled,
            float displayGamma) {
        int w = maxX - minX + 1;
        int h = maxY - minY + 1;
        TileLight[][] cache = new TileLight[w][h];
        if (!localLightingEnabled) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    cache[x][y] = TileLight.fullWhite();
                }
            }
            return cache;
        }
        float minLightLevel = config.minLightLevel;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                float[] rgb =
                        computeRgb(
                                world,
                                x,
                                y,
                                timeOfDay,
                                config,
                                dayNightEnabled,
                                minLightLevel,
                                displayGammaEnabled,
                                displayGamma);
                cache[x - minX][y - minY] = TileLight.fromRgb(rgb);
            }
        }
        return cache;
    }

    private static float[] computeRgb(
            World world,
            int cellX,
            int cellY,
            float timeOfDay,
            DayNightConfig config,
            boolean dayNightEnabled,
            float minLightLevel,
            boolean displayGammaEnabled,
            float displayGamma) {
        float ambientLevel =
                dayNightEnabled
                        ? AmbientLighting.ambientLevel(timeOfDay, config)
                                * AmbientLighting.sunFactorAt(cellX, cellY, world)
                        : 1f;
        float[] ambientChroma =
                dayNightEnabled ? AmbientLighting.ambientChroma(timeOfDay, config) : WHITE_CHROMA;
        float[] ambientBaseline = LightColor.scale(ambientLevel, ambientChroma);

        BlockId object = world.getObject(cellX, cellY);
        if (BlockDefinitions.lightEmission(object) > 0f) {
            float[] rgb =
                    LightColor.raiseChannelsToBaseline(
                            emissiveRgb(object, ambientChroma), ambientBaseline);
            return finishRgb(
                    rgb,
                    minLightLevel,
                    displayGammaEnabled,
                    displayGamma);
        }

        float blockLevel = world.lightMap().sample(cellX, cellY);
        float[] blockChroma = LightColor.normalizeChroma(world.lightMap().sampleColor(cellX, cellY));

        float level = LightColor.maxLevel(ambientLevel, blockLevel);
        float chromaWeight = blockLevel / Math.max(level, 1e-5f);
        float[] chroma = LightColor.blendChroma(ambientChroma, blockChroma, chromaWeight);

        float[] rgb =
                LightColor.raiseChannelsToBaseline(LightColor.scale(level, chroma), ambientBaseline);
        return finishRgb(rgb, minLightLevel, displayGammaEnabled, displayGamma);
    }

    /**
     * Emitter sprite tint: {@link BlockDefinitions#lightEmission} drives level; chroma shifts toward
     * source color. Skips propagated block light on the source cell (ring uses normal combine).
     */
    private static float[] emissiveRgb(BlockId object, float[] ambientChroma) {
        float emission = BlockDefinitions.lightEmission(object);
        float[] blockChroma = LightColor.normalizeChroma(BlockDefinitions.lightColor(object));
        float chromaWeight = Math.min(1f, emission);
        float[] chroma = LightColor.blendChroma(ambientChroma, blockChroma, chromaWeight);
        return LightColor.scale(emission, chroma);
    }

    private static float[] finishRgb(
            float[] rgb, float minLightLevel, boolean displayGammaEnabled, float displayGamma) {
        float[] floored = LightColor.applyMinLightLevel(rgb, minLightLevel);
        return LightColor.applyDisplayGamma(floored, displayGammaEnabled, displayGamma);
    }
}
