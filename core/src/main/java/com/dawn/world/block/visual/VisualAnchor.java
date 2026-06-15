package com.dawn.world.block.visual;

/**
 * Which point on the occupancy cell the sprite is aligned to before {@link BlockVisualDef#offsetPxX()} /
 * {@link BlockVisualDef#offsetPxY()}.
 *
 * <p>Sprite extends upward from the anchor Y (LibGDX bottom-left origin).
 */
public enum VisualAnchor {
    /** Bottom-left corner of the cell — good for crates, bushes, floor-aligned props. */
    CELL_BOTTOM_LEFT,
    /** Bottom edge, horizontally centered — good for tall centered trees. */
    CELL_BOTTOM_CENTER,
    /** Geometric center of the cell — good for oversized ground tiles with border overlap. */
    CELL_CENTER
}
