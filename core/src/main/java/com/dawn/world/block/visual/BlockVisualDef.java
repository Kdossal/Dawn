package com.dawn.world.block.visual;

import com.dawn.assets.BlockTextureId;
import com.dawn.config.Constants;

/**
 * Presentation for a block sprite: pixel size, anchor on the occupancy cell, and optional nudge.
 *
 * <p>Dominant authoring path: {@link BlockVisualDefinitionsLoader} + {@code block_visuals.json}. Use {@link
 * BlockVisualPresets} for shorthand in tests / tools. Draw order is Y-sort, not metadata here.
 */
public record BlockVisualDef(
        BlockTextureId texture,
        int widthPx,
        int heightPx,
        VisualAnchor anchor,
        int offsetPxX,
        int offsetPxY,
        float defaultAlpha) {

    /** Full 16×16 tile anchored to the cell bottom-left corner. */
    public static BlockVisualDef cellTile(BlockTextureId texture) {
        return of(
                texture,
                Constants.TILE_ART_PX,
                Constants.TILE_ART_PX,
                VisualAnchor.CELL_BOTTOM_LEFT,
                0,
                0,
                1f);
    }

    public static BlockVisualDef cellArt(
            BlockTextureId texture, int widthPx, int heightPx, VisualAnchor anchor) {
        return of(texture, widthPx, heightPx, anchor, 0, 0, 1f);
    }

    public static BlockVisualDef cellArt(
            BlockTextureId texture,
            int widthPx,
            int heightPx,
            VisualAnchor anchor,
            int offsetPxX,
            int offsetPxY,
            float defaultAlpha) {
        return of(texture, widthPx, heightPx, anchor, offsetPxX, offsetPxY, defaultAlpha);
    }

    public static BlockVisualDef of(
            BlockTextureId texture,
            int widthPx,
            int heightPx,
            VisualAnchor anchor,
            int offsetPxX,
            int offsetPxY,
            float defaultAlpha) {
        return new BlockVisualDef(
                texture, widthPx, heightPx, anchor, offsetPxX, offsetPxY, defaultAlpha);
    }

    /** Extra cells to include when culling visible region (left, right, down, up). */
    public int[] cullPaddingCells() {
        if (anchor == VisualAnchor.CELL_CENTER) {
            return symmetricCullPaddingCells();
        }
        int cell = Constants.CELL_SIZE_PX;
        int padLeft = Math.max(0, (widthPx - cell) / 2 + Math.max(0, -offsetPxX) + cell - 1) / cell;
        int padRight = Math.max(0, (widthPx - cell) / 2 + Math.max(0, offsetPxX) + cell - 1) / cell;
        int padDown = Math.max(0, Math.max(0, -offsetPxY) + cell - 1) / cell;
        int overflowUp = heightPx - cell + Math.max(0, offsetPxY);
        int padUp = Math.max(0, (overflowUp + cell - 1) / cell);
        return new int[] {padLeft, padRight, padDown, padUp};
    }

    private int[] symmetricCullPaddingCells() {
        int cell = Constants.CELL_SIZE_PX;
        int bleedX = Math.max(0, (widthPx - cell) / 2 + Math.abs(offsetPxX));
        int bleedY = Math.max(0, (heightPx - cell) / 2 + Math.abs(offsetPxY));
        int padX = Math.max(0, (bleedX + cell - 1) / cell);
        int padY = Math.max(0, (bleedY + cell - 1) / cell);
        return new int[] {padX, padX, padY, padY};
    }
}
