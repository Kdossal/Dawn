package com.dawn.world.block.visual;

import com.dawn.config.Constants;
import com.dawn.render.SpriteAnchor;

/** Pixel draw rectangle for a block visual at a cell. */
public final class BlockVisualLayout {
    private BlockVisualLayout() {}

    /** @return {@code [px, py, width, height]} */
    public static float[] rectPx(BlockVisualDef visual, int cellX, int cellY) {
        float w = visual.widthPx();
        float h = visual.heightPx();
        float[] origin = switch (visual.anchor()) {
            case CELL_BOTTOM_LEFT -> SpriteAnchor.cellBottomLeft(cellX, cellY, w, h);
            case CELL_BOTTOM_CENTER -> SpriteAnchor.cellBottomCenter(cellX, cellY, w, h);
            case CELL_CENTER -> SpriteAnchor.cellCenter(cellX, cellY, w, h);
        };
        float px = origin[0] + visual.offsetPxX();
        float py = origin[1] + visual.offsetPxY();
        return new float[] {px, py, w, h};
    }

    /**
     * Depth-sort anchor in cell space: bottom edge Y and right edge X of the drawn sprite (LibGDX bottom-left
     * origin).
     *
     * @return {@code [sortY, sortX]}
     */
    public static float[] bottomRightCell(BlockVisualDef visual, int cellX, int cellY) {
        float[] rect = rectPx(visual, cellX, cellY);
        float invCell = 1f / Constants.CELL_SIZE_PX;
        float sortY = rect[1] * invCell;
        float sortX = (rect[0] + rect[2]) * invCell;
        return new float[] {sortY, sortX};
    }

    /** Sprite AABB in cell space: {@code [left, bottom, right, top]}. */
    public static float[] spriteFootprintCell(BlockVisualDef visual, int cellX, int cellY) {
        float[] rect = rectPx(visual, cellX, cellY);
        float invCell = 1f / Constants.CELL_SIZE_PX;
        return new float[] {
            rect[0] * invCell,
            rect[1] * invCell,
            (rect[0] + rect[2]) * invCell,
            (rect[1] + rect[3]) * invCell
        };
    }
}
