package com.dawn.world.block.visual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.render.BatchDraw;
import com.dawn.render.LitQuadDraw;
import com.dawn.render.TileLightCorners;
import com.dawn.render.TileLighting;

/** Draws block visuals at world pixel positions. */
public final class BlockSpriteDraw {
    private BlockSpriteDraw() {}

    public static void drawBlock(
            SpriteBatch batch, DawnAssets assets, BlockVisualDef visual, int cellX, int cellY) {
        drawBlock(batch, assets, visual, cellX, cellY, 1f, 0f, 0f);
    }

    public static void drawBlock(
            SpriteBatch batch,
            DawnAssets assets,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float alphaMultiplier) {
        drawBlock(batch, assets, visual, cellX, cellY, alphaMultiplier, 0f, 0f);
    }

    public static void drawBlock(
            SpriteBatch batch,
            DawnAssets assets,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float alphaMultiplier,
            float alignOffsetX,
            float alignOffsetY) {
        drawBrightBlock(batch, assets, visual, cellX, cellY, 1f, alphaMultiplier, alignOffsetX, alignOffsetY);
    }

    public static void drawBrightBlock(
            SpriteBatch batch,
            DawnAssets assets,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float brightness,
            float alphaMultiplier,
            float alignOffsetX,
            float alignOffsetY) {
        TextureRegion region = assets.tile(visual.texture());
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        float alpha = visual.defaultAlpha() * alphaMultiplier;
        batch.setColor(brightness, brightness, brightness, alpha);
        batch.draw(region, rect[0] + alignOffsetX, rect[1] + alignOffsetY, rect[2], rect[3]);
        batch.setColor(Color.WHITE);
    }

    public static void drawColoredBlock(
            SpriteBatch batch,
            DawnAssets assets,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float r,
            float g,
            float b,
            float alphaMultiplier,
            float alignOffsetX,
            float alignOffsetY) {
        drawColoredBlockCorners(
                batch,
                assets,
                visual,
                cellX,
                cellY,
                TileLightCorners.uniform(new TileLighting.TileLight(r, g, b)),
                alphaMultiplier,
                alignOffsetX,
                alignOffsetY);
    }

    public static void drawColoredBlockCorners(
            SpriteBatch batch,
            DawnAssets assets,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            TileLightCorners corners,
            float alphaMultiplier,
            float alignOffsetX,
            float alignOffsetY) {
        TextureRegion region = assets.tile(visual.texture());
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        float alpha = visual.defaultAlpha() * alphaMultiplier;
        float x = rect[0] + alignOffsetX;
        float y = rect[1] + alignOffsetY;
        LitQuadDraw.drawRegion(batch, region, x, y, rect[2], rect[3], corners, alpha, false);
    }

    public static void drawTintedBlock(
            SpriteBatch batch,
            DawnAssets assets,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            Color tint) {
        drawTintedBlock(batch, assets, visual, cellX, cellY, tint, 0f, 0f);
    }

    public static void drawTintedBlock(
            SpriteBatch batch,
            DawnAssets assets,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            Color tint,
            float alignOffsetX,
            float alignOffsetY) {
        TextureRegion region = assets.tile(visual.texture());
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        batch.setColor(tint.r, tint.g, tint.b, tint.a);
        batch.draw(region, rect[0] + alignOffsetX, rect[1] + alignOffsetY, rect[2], rect[3]);
        batch.setColor(Color.WHITE);
    }

    public static void drawRegion(
            SpriteBatch batch,
            DawnAssets assets,
            TextureRegion region,
            BlockVisualDef visual,
            int cellX,
            int cellY) {
        drawRegion(batch, assets, region, visual, cellX, cellY, 0f, 0f);
    }

    public static void drawRegion(
            SpriteBatch batch,
            DawnAssets assets,
            TextureRegion region,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float alignOffsetX,
            float alignOffsetY) {
        drawBrightRegion(batch, assets, region, visual, cellX, cellY, 1f, alignOffsetX, alignOffsetY);
    }

    public static void drawBrightRegion(
            SpriteBatch batch,
            DawnAssets assets,
            TextureRegion region,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float brightness,
            float alignOffsetX,
            float alignOffsetY) {
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        float alpha = visual.defaultAlpha();
        batch.setColor(brightness, brightness, brightness, alpha);
        batch.draw(region, rect[0] + alignOffsetX, rect[1] + alignOffsetY, rect[2], rect[3]);
        batch.setColor(Color.WHITE);
    }

    public static void drawColoredRegion(
            SpriteBatch batch,
            DawnAssets assets,
            TextureRegion region,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float r,
            float g,
            float b,
            float alignOffsetX,
            float alignOffsetY) {
        drawColoredRegion(batch, assets, region, visual, cellX, cellY, r, g, b, 1f, alignOffsetX, alignOffsetY);
    }

    public static void drawColoredRegion(
            SpriteBatch batch,
            DawnAssets assets,
            TextureRegion region,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            float r,
            float g,
            float b,
            float alphaMultiplier,
            float alignOffsetX,
            float alignOffsetY) {
        drawColoredRegionCorners(
                batch,
                assets,
                region,
                visual,
                cellX,
                cellY,
                TileLightCorners.uniform(new TileLighting.TileLight(r, g, b)),
                alphaMultiplier,
                alignOffsetX,
                alignOffsetY);
    }

    public static void drawColoredRegionCorners(
            SpriteBatch batch,
            DawnAssets assets,
            TextureRegion region,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            TileLightCorners corners,
            float alphaMultiplier,
            float alignOffsetX,
            float alignOffsetY) {
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        float alpha = visual.defaultAlpha() * alphaMultiplier;
        float x = rect[0] + alignOffsetX;
        float y = rect[1] + alignOffsetY;
        LitQuadDraw.drawRegion(batch, region, x, y, rect[2], rect[3], corners, alpha, false);
    }

    public static void drawTintedRegion(
            SpriteBatch batch,
            DawnAssets assets,
            TextureRegion region,
            BlockVisualDef visual,
            int cellX,
            int cellY,
            Color tint,
            float alignOffsetX,
            float alignOffsetY) {
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        float alpha = visual.defaultAlpha() * tint.a;
        batch.setColor(tint.r, tint.g, tint.b, alpha);
        batch.draw(region, rect[0] + alignOffsetX, rect[1] + alignOffsetY, rect[2], rect[3]);
        batch.setColor(Color.WHITE);
    }

    public static void drawTintedCell(
            SpriteBatch batch, DawnAssets assets, int cellX, int cellY, Color tint) {
        drawTintedCell(batch, assets, cellX, cellY, tint, 0f, 0f);
    }

    public static void drawTintedCell(
            SpriteBatch batch,
            DawnAssets assets,
            int cellX,
            int cellY,
            Color tint,
            float alignOffsetX,
            float alignOffsetY) {
        float px = cellX * Constants.CELL_SIZE_PX + alignOffsetX;
        float py = cellY * Constants.CELL_SIZE_PX + alignOffsetY;
        BatchDraw.tintedRect(
                batch, assets.whitePixel, px, py, Constants.CELL_SIZE_PX, Constants.CELL_SIZE_PX, tint);
    }
}
