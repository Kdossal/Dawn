package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EightNeighborMaskTest {
    private World world;

    @BeforeEach
    void setUp() {
        world = TestWorlds.smallWalkable(5, 5);
    }

    @Test
    void compute_includesDiagonalWhenSameBlock() {
        world.setGround(2, 2, BlockId.WATER);
        world.setGround(3, 2, BlockId.WATER);
        world.setGround(2, 1, BlockId.WATER);
        world.setGround(3, 1, BlockId.WATER);
        int mask =
                EightNeighborMask.compute(world, 2, 2, com.dawn.world.block.Layer.GROUND, BlockId.WATER);
        assertEquals(
                EightNeighborMask.EAST | EightNeighborMask.SOUTH | EightNeighborMask.SOUTH_EAST,
                mask);
    }
}
