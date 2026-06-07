package com.dawn.world.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dawn.assets.DawnAssets;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.visual.BlockSpriteDraw;
import com.dawn.world.block.visual.BlockVisualDef;
import com.dawn.world.block.visual.BlockVisualRegistry;

public final class BlockWorldDrawable implements WorldDrawable {
    private final BlockId blockId;
    private final int cellX;
    private final int cellY;
    private final float sortY;
    private final float sortX;

    public BlockWorldDrawable(BlockId blockId, int cellX, int cellY) {
        this.blockId = blockId;
        this.cellX = cellX;
        this.cellY = cellY;
        BlockVisualDef visual = BlockVisualRegistry.get(blockId);
        float[] key = WorldSortKeys.blockBottomRight(visual, cellX, cellY);
        this.sortY = key[0];
        this.sortX = key[1];
    }

    public BlockId blockId() {
        return blockId;
    }

    public int cellX() {
        return cellX;
    }

    public int cellY() {
        return cellY;
    }

    @Override
    public float sortY() {
        return sortY;
    }

    @Override
    public float sortX() {
        return sortX;
    }

    @Override
    public int depthBias() {
        return WorldDepthBias.block(blockId);
    }

    @Override
    public void draw(SpriteBatch batch, DawnAssets assets, DrawContext context) {
        BlockVisualDef visual = BlockVisualRegistry.get(blockId);
        if (visual != null) {
            float alpha = context.fadePlan().blockDrawAlpha(blockId, cellX, cellY);
            BlockSpriteDraw.drawBlock(
                    batch,
                    assets,
                    visual,
                    cellX,
                    cellY,
                    alpha,
                    context.pixelAlignOffsetX(),
                    context.pixelAlignOffsetY());
        }
    }
}
