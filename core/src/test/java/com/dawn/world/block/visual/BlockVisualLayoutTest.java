package com.dawn.world.block.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.assets.BlockTextureId;
import com.dawn.config.Constants;
import org.junit.jupiter.api.Test;

class BlockVisualLayoutTest {

    @Test
    void crate_drawsAtFull16x24_onCellBottomLeft() {
        BlockVisualDef crate = BlockVisualRegistry.get(com.dawn.world.block.BlockId.CRATE);
        float[] rect = BlockVisualLayout.rectPx(crate, 3, 5);
        assertEquals(3 * Constants.CELL_SIZE_PX, rect[0], 1e-5f);
        assertEquals(5 * Constants.CELL_SIZE_PX, rect[1], 1e-5f);
        assertEquals(16f, rect[2], 1e-5f);
        assertEquals(20f, rect[3], 1e-5f);
    }

    @Test
    void customShortBush_example10pxTall() {
        BlockVisualDef bush =
                BlockVisualDef.cellArt(
                        com.dawn.assets.BlockTextureId.BUSH, 16, 10, VisualAnchor.CELL_BOTTOM_LEFT);
        float[] rect = BlockVisualLayout.rectPx(bush, 0, 0);
        assertEquals(16f, rect[2], 1e-5f);
        assertEquals(10f, rect[3], 1e-5f);
    }

    @Test
    void preset_propBottomLeft_matchesEquivalentCellArt() {
        BlockVisualDef a = BlockVisualPresets.propBottomLeft(BlockTextureId.CRATE, 16, 28);
        BlockVisualDef b = BlockVisualDef.cellArt(BlockTextureId.CRATE, 16, 28, VisualAnchor.CELL_BOTTOM_LEFT);
        assertEquals(a.widthPx(), b.widthPx());
        assertEquals(a.heightPx(), b.heightPx());
        assertEquals(a.anchor(), b.anchor());
    }

    @Test
    void cellCenter24x24_isCenteredOnCell() {
        BlockVisualDef ground =
                BlockVisualDef.cellArt(BlockTextureId.DIRT, 24, 24, VisualAnchor.CELL_CENTER);
        float[] rect = BlockVisualLayout.rectPx(ground, 3, 5);
        assertEquals(3 * Constants.CELL_SIZE_PX - 4f, rect[0], 1e-5f);
        assertEquals(5 * Constants.CELL_SIZE_PX - 4f, rect[1], 1e-5f);
        assertEquals(24f, rect[2], 1e-5f);
        assertEquals(24f, rect[3], 1e-5f);
    }

    @Test
    void cellCenter24x24_cullPaddingOneCellAllSides() {
        BlockVisualDef ground =
                BlockVisualDef.cellArt(BlockTextureId.STONE, 24, 24, VisualAnchor.CELL_CENTER);
        int[] pad = ground.cullPaddingCells();
        assertEquals(1, pad[0]);
        assertEquals(1, pad[1]);
        assertEquals(1, pad[2]);
        assertEquals(1, pad[3]);
    }
}
