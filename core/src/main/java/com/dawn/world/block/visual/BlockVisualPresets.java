package com.dawn.world.block.visual;

import com.dawn.assets.BlockTextureId;
import com.dawn.config.Constants;

/** Named recipes for common {@link BlockVisualDef} shapes; JSON mirrors these conventions. */
public final class BlockVisualPresets {
    private BlockVisualPresets() {}

    /** Base / floor / ordinary block: 16×16 anchored to cell bottom-left. */
    public static BlockVisualDef tiledGround(BlockTextureId texture) {
        return BlockVisualDef.cellTile(texture);
    }

    /** Ground-aligned prop (crate, stump) with arbitrary pixel size. */
    public static BlockVisualDef propBottomLeft(BlockTextureId texture, int widthPx, int heightPx) {
        return BlockVisualDef.cellArt(texture, widthPx, heightPx, VisualAnchor.CELL_BOTTOM_LEFT);
    }

    /** Tall sprites centered horizontally on cell floor (trees, poles). */
    public static BlockVisualDef tallBottomCenter(BlockTextureId texture, int widthPx, int heightPx, float alpha) {
        return BlockVisualDef.cellArt(
                texture, widthPx, heightPx, VisualAnchor.CELL_BOTTOM_CENTER, 0, 0, alpha);
    }

    /** Full-cell ground overlay (bushes): 16×16 bottom-left with optional alpha. */
    public static BlockVisualDef bushLike(BlockTextureId texture, float alpha) {
        return BlockVisualDef.cellArt(
                texture,
                Constants.TILE_ART_PX,
                Constants.TILE_ART_PX,
                VisualAnchor.CELL_BOTTOM_LEFT,
                0,
                0,
                alpha);
    }
}
