package com.dawn.world.block.autotile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.world.World;
import com.dawn.world.block.visual.BlockSpriteDraw;
import com.dawn.world.block.visual.BlockVisualDef;

/** Resolves and draws an autotile cell at world coordinates. */
public final class AutotileDraw {
    private AutotileDraw() {}

    public static void drawTinted(
            SpriteBatch batch,
            DawnAssets assets,
            World world,
            AutotileFamily family,
            int cellX,
            int cellY,
            BlockVisualDef visual,
            Color tint,
            float alignOffsetX,
            float alignOffsetY) {
        AutotileCell cell = AutotileResolver.resolve(world, cellX, cellY, family);
        TextureRegion region = assets.autotileRegion(family.texture(), cell);
        BlockSpriteDraw.drawTintedRegion(
                batch, assets, region, visual, cellX, cellY, tint, alignOffsetX, alignOffsetY);
    }

    public static void drawColored(
            SpriteBatch batch,
            DawnAssets assets,
            World world,
            AutotileFamily family,
            int cellX,
            int cellY,
            BlockVisualDef visual,
            float r,
            float g,
            float b,
            float alpha,
            float alignOffsetX,
            float alignOffsetY) {
        AutotileCell cell = AutotileResolver.resolve(world, cellX, cellY, family);
        TextureRegion region = assets.autotileRegion(family.texture(), cell);
        BlockSpriteDraw.drawColoredRegion(
                batch,
                assets,
                region,
                visual,
                cellX,
                cellY,
                r,
                g,
                b,
                alpha,
                alignOffsetX,
                alignOffsetY);
    }
}
