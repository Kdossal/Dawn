package com.dawn.world.block.visual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.render.BatchDraw;

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
        TextureRegion region = assets.tile(visual.texture());
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        float drawW = rect[2];
        float drawH = rect[3];
        float alpha = visual.defaultAlpha() * alphaMultiplier;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(region, rect[0] + alignOffsetX, rect[1] + alignOffsetY, drawW, drawH);
        batch.setColor(Color.WHITE);
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
        if (region == null) {
            return;
        }
        float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
        float alpha = visual.defaultAlpha();
        batch.setColor(1f, 1f, 1f, alpha);
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
