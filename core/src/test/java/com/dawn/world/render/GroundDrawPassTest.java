package com.dawn.world.render;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class GroundDrawPassTest {

    @Test
    void forGround_pitAndWater_areFluid() {
        assertEquals(GroundDrawPass.FLUID, GroundDrawPass.forGround(BlockId.PIT));
        assertEquals(GroundDrawPass.FLUID, GroundDrawPass.forGround(BlockId.WATER));
    }

    @Test
    void forGround_solidGrounds_mapToMaterialPasses() {
        assertEquals(GroundDrawPass.STONE, GroundDrawPass.forGround(BlockId.STONE_GROUND));
        assertEquals(GroundDrawPass.SAND, GroundDrawPass.forGround(BlockId.SAND_GROUND));
        assertEquals(GroundDrawPass.DIRT, GroundDrawPass.forGround(BlockId.DIRT_GROUND));
    }

    @Test
    void forGround_nonGround_returnsNull() {
        assertNull(GroundDrawPass.forGround(BlockId.GRASS));
        assertNull(GroundDrawPass.forGround(BlockId.ROCK));
        assertNull(GroundDrawPass.forGround(BlockId.AIR));
    }

    @Test
    void matches_onlyOwnBlockIds() {
        assertTrue(GroundDrawPass.FLUID.matches(BlockId.WATER));
        assertFalse(GroundDrawPass.FLUID.matches(BlockId.STONE_GROUND));
        assertTrue(GroundDrawPass.DIRT.matches(BlockId.DIRT_GROUND));
        assertFalse(GroundDrawPass.DIRT.matches(BlockId.SAND_GROUND));
    }

    @Test
    void isFloorBlock_grassOnly() {
        assertTrue(GroundDrawPass.isFloorBlock(BlockId.GRASS));
        assertFalse(GroundDrawPass.isFloorBlock(BlockId.DIRT_GROUND));
        assertFalse(GroundDrawPass.isFloorBlock(BlockId.AIR));
    }

    @Test
    void groundPasses_orderIsBackToFront() {
        GroundDrawPass[] passes = GroundDrawPass.groundPasses();
        assertEquals(GroundDrawPass.FLUID, passes[0]);
        assertEquals(GroundDrawPass.STONE, passes[1]);
        assertEquals(GroundDrawPass.SAND, passes[2]);
        assertEquals(GroundDrawPass.DIRT, passes[3]);
    }
}
