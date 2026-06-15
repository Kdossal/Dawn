package com.dawn.world.block.autotile;

/**
 * STANDARD_BLOB paint grid: each mask is which cardinal neighbors share the tile (N=8,E=4,S=2,W=1).
 * Cell positions match painted art in the sheet — open side is the direction with no neighbor bit.
 */
public final class StandardBlobLayout {
    private static final int[][] MASK_CELLS = {
        {3, 3}, // 0  isolated
        {2, 3}, // 1  W only → right U
        {3, 0}, // 2  S only → top U
        {2, 0}, // 3  SW → NE corner art
        {0, 3}, // 4  E only → left U
        {1, 3}, // 5  E+W → horizontal
        {0, 0}, // 6  SE → NW corner art
        {1, 0}, // 7  S+E+W → N edge (open north)
        {3, 2}, // 8  N only → bottom U
        {2, 2}, // 9  NW → SE corner art
        {3, 1}, // 10 N+S → vertical
        {2, 1}, // 11 N+S+W → E edge (open east)
        {0, 2}, // 12 NE → SW corner art
        {1, 2}, // 13 N+E+W → S edge (open south)
        {0, 1}, // 14 N+E+S → W edge (open west)
    };

    private StandardBlobLayout() {}

    public static AutotileCell cellForMask(int mask) {
        if (mask < 0 || mask >= AutotileFamily.FULL_SURROUND_MASK) {
            throw new IllegalArgumentException("Standard blob has no tile for mask " + mask);
        }
        int[] cell = MASK_CELLS[mask];
        return new AutotileCell(cell[0], cell[1]);
    }

    /** Interior tile for 4×4 sheets without a dedicated center variant row. */
    public static AutotileCell fullSurroundCell() {
        return new AutotileCell(1, 1);
    }

    /** Masks 0–14 filled; index 15 left null for center variant selection. */
    public static AutotileCell[] buildMaskTiles() {
        AutotileCell[] maskTiles = new AutotileCell[AutotileFamily.MASK_COUNT];
        for (int mask = 0; mask < AutotileFamily.FULL_SURROUND_MASK; mask++) {
            maskTiles[mask] = cellForMask(mask);
        }
        return maskTiles;
    }

    public static AutotileCell[] centerTilesFromRow(int cols, int centerRow) {
        AutotileCell[] cells = new AutotileCell[cols];
        for (int col = 0; col < cols; col++) {
            cells[col] = new AutotileCell(col, centerRow);
        }
        return cells;
    }
}
