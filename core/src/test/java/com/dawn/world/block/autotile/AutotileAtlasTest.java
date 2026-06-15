package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class AutotileAtlasTest {

    @Test
    void stoneWallFamily_expectsRectangularSheetDimensions() {
        AutotileFamily wall = AutotileDefinitionsLoader.load().get(BlockId.STONE_WALL);
        assertNotNull(wall);
        assertEquals(16, wall.tileWidthPx());
        assertEquals(32, wall.tileHeightPx());
        assertEquals(64, wall.cols() * wall.tileWidthPx());
        assertEquals(128, wall.rows() * wall.tileHeightPx());
    }
}
