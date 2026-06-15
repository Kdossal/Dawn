package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StandardBlobLayoutTest {
    @Test
    void cellForMask_isolated() {
        assertCell(0, 3, 3);
    }

    @Test
    void cellForMask_nEdge_openNorth() {
        assertCell(7, 1, 0);
    }

    @Test
    void cellForMask_eEdge_openEast() {
        assertCell(11, 2, 1);
    }

    @Test
    void cellForMask_topU_southOnly() {
        assertCell(2, 3, 0);
    }

    @Test
    void cellForMask_seCorner() {
        assertCell(6, 0, 0);
    }

    @Test
    void cellForMask_nwCorner() {
        assertCell(9, 2, 2);
    }

    @Test
    void cellForMask_bottomU_northOnly() {
        assertCell(8, 3, 2);
    }

    @Test
    void cellForMask_wEdge_openWest() {
        assertCell(14, 0, 1);
    }

    @Test
    void cellForMask_sEdge_openSouth() {
        assertCell(13, 1, 2);
    }

    @Test
    void fullSurroundCell_isCenterOfThreeByThree() {
        AutotileCell cell = StandardBlobLayout.fullSurroundCell();
        assertEquals(1, cell.col());
        assertEquals(1, cell.row());
    }

    @Test
    void buildMaskTiles_fillsZeroThroughFourteen() {
        AutotileCell[] tiles = StandardBlobLayout.buildMaskTiles();
        assertEquals(16, tiles.length);
        for (int mask = 0; mask < AutotileFamily.FULL_SURROUND_MASK; mask++) {
            assertEquals(StandardBlobLayout.cellForMask(mask), tiles[mask]);
        }
        assertNull(tiles[15]);
    }

    @Test
    void centerTilesFromRow_spansColumns() {
        AutotileCell[] cells = StandardBlobLayout.centerTilesFromRow(4, 4);
        assertEquals(4, cells.length);
        assertEquals(new AutotileCell(0, 4), cells[0]);
        assertEquals(new AutotileCell(3, 4), cells[3]);
    }

    private static void assertCell(int mask, int col, int row) {
        AutotileCell cell = StandardBlobLayout.cellForMask(mask);
        assertEquals(col, cell.col());
        assertEquals(row, cell.row());
    }
}
