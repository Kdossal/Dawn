package com.dawn.world.render;

import com.dawn.config.Constants;
import com.dawn.gameplay.drops.DropRenderer;
import com.dawn.gameplay.drops.WorldDrop;
import com.dawn.world.block.visual.BlockVisualDef;
import com.dawn.world.block.visual.BlockVisualLayout;

/** Depth-sort points in cell space (bottom Y, right X). */
public final class WorldSortKeys {
    private WorldSortKeys() {}

    public static float[] blockBottomRight(BlockVisualDef visual, int cellX, int cellY) {
        if (visual == null) {
            return new float[] {cellY, cellX + 1f};
        }
        return BlockVisualLayout.bottomRightCell(visual, cellX, cellY);
    }

    /** Feet bottom Y and sprite right edge X in cells. */
    public static float[] entityBottomRight(float feetX, float feetY, int spriteWidthPx) {
        float sortY = feetY;
        float halfWidthCells = spriteWidthPx / (2f * Constants.CELL_SIZE_PX);
        float sortX = feetX + halfWidthCells;
        return new float[] {sortY, sortX};
    }

    public static float[] dropBottomRight(WorldDrop drop) {
        float halfIconCells = DropRenderer.iconHalfSizeCells();
        float sortY = drop.y - halfIconCells;
        float sortX = drop.x + halfIconCells;
        return new float[] {sortY, sortX};
    }
}
