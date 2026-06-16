package com.dawn.world.render;

import com.dawn.assets.DawnAssets;
import com.dawn.config.DayNightConfig;
import com.dawn.entity.EntityBounds;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.dawn.render.LightColor;
import com.dawn.render.TileLightCorners;
import com.dawn.render.TileLighting;
import com.dawn.world.World;
import java.util.List;

/** Per-frame draw state for Y-sorted world sprites. */
public final class DrawContext {
    private final World world;
    private final OcclusionFadePlan fadePlan;
    private final float pixelAlignOffsetX;
    private final float pixelAlignOffsetY;
    private final TileLighting.TileLight[][] lightCache;
    private final int cacheMinX;
    private final int cacheMinY;
    private final int cacheWidth;
    private final int cacheHeight;
    private final float timeOfDay;
    private final DayNightConfig dayNightConfig;
    private final boolean localLightingEnabled;
    private final boolean dayNightEnabled;
    private final boolean displayGammaEnabled;
    private final float displayGamma;

    private DrawContext(
            World world,
            OcclusionFadePlan fadePlan,
            float pixelAlignOffsetX,
            float pixelAlignOffsetY,
            TileLighting.TileLight[][] lightCache,
            int cacheMinX,
            int cacheMinY,
            float timeOfDay,
            DayNightConfig dayNightConfig,
            boolean localLightingEnabled,
            boolean dayNightEnabled,
            boolean displayGammaEnabled,
            float displayGamma) {
        this.world = world;
        this.fadePlan = fadePlan;
        this.pixelAlignOffsetX = pixelAlignOffsetX;
        this.pixelAlignOffsetY = pixelAlignOffsetY;
        this.lightCache = lightCache;
        this.cacheMinX = cacheMinX;
        this.cacheMinY = cacheMinY;
        this.cacheWidth = lightCache == null ? 0 : lightCache.length;
        this.cacheHeight = lightCache == null || lightCache.length == 0 ? 0 : lightCache[0].length;
        this.timeOfDay = timeOfDay;
        this.dayNightConfig = dayNightConfig;
        this.localLightingEnabled = localLightingEnabled;
        this.dayNightEnabled = dayNightEnabled;
        this.displayGammaEnabled = displayGammaEnabled;
        this.displayGamma = displayGamma;
    }

    public static DrawContext create(
            World world,
            List<WorldDrawable> drawables,
            EntityBounds playerBounds,
            float playerFeetX,
            float playerFeetY,
            EntitySpriteFrame playerSprite,
            DawnAssets assets,
            boolean occlusionFadeEnabled,
            float pixelAlignOffsetX,
            float pixelAlignOffsetY,
            int minX,
            int minY,
            int maxX,
            int maxY,
            float timeOfDay,
            DayNightConfig dayNightConfig,
            boolean localLightingEnabled,
            boolean dayNightEnabled,
            boolean displayGammaEnabled,
            float displayGamma) {
        OcclusionFadePlan fadePlan =
                occlusionFadeEnabled
                        ? OcclusionFadePlan.build(
                                drawables,
                                playerBounds,
                                playerFeetX,
                                playerFeetY,
                                playerSprite,
                                assets)
                        : OcclusionFadePlan.disabled();
        TileLighting.TileLight[][] cache =
                TileLighting.buildCache(
                        world,
                        minX,
                        minY,
                        maxX,
                        maxY,
                        timeOfDay,
                        dayNightConfig,
                        localLightingEnabled,
                        dayNightEnabled,
                        displayGammaEnabled,
                        displayGamma);
        return new DrawContext(
                world,
                fadePlan,
                pixelAlignOffsetX,
                pixelAlignOffsetY,
                cache,
                minX,
                minY,
                timeOfDay,
                dayNightConfig,
                localLightingEnabled,
                dayNightEnabled,
                displayGammaEnabled,
                displayGamma);
    }

    public World world() {
        return world;
    }

    public OcclusionFadePlan fadePlan() {
        return fadePlan;
    }

    public float pixelAlignOffsetX() {
        return pixelAlignOffsetX;
    }

    public float pixelAlignOffsetY() {
        return pixelAlignOffsetY;
    }

    public TileLighting.TileLight tileLight(int cellX, int cellY) {
        if (lightCache == null || !localLightingEnabled) {
            return TileLighting.TileLight.fullWhite();
        }
        int lx = cellX - cacheMinX;
        int ly = cellY - cacheMinY;
        if (lx >= 0 && ly >= 0 && lx < cacheWidth && ly < cacheHeight) {
            return lightCache[lx][ly];
        }
        return TileLighting.sample(
                world,
                cellX,
                cellY,
                timeOfDay,
                dayNightConfig,
                localLightingEnabled,
                dayNightEnabled,
                displayGammaEnabled,
                displayGamma);
    }

    public TileLightCorners tileLightCorners(int cellX, int cellY) {
        return TileLighting.sampleCornersFromCache(
                world,
                cellX,
                cellY,
                timeOfDay,
                dayNightConfig,
                localLightingEnabled,
                dayNightEnabled,
                displayGammaEnabled,
                displayGamma,
                lightCache,
                cacheMinX,
                cacheMinY);
    }

    public float tileBrightness(int cellX, int cellY) {
        TileLighting.TileLight light = tileLight(cellX, cellY);
        return LightColor.maxChannel(new float[] {light.r(), light.g(), light.b()});
    }
}
