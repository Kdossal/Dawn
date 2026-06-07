package com.dawn.gameplay.drops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class DropPlacementTest {
    @Test
    void cellCenter_prefersWalkableNeighborOverPit() {
        World world = TestWorlds.smallWalkable(8, 8);
        world.setGround(5, 4, BlockId.PIT);
        float[] center = DropPlacement.cellCenter(world, 5, 4);
        assertEquals(6.5f, center[0], 0.001f);
        assertEquals(4.5f, center[1], 0.001f);
    }

    @Test
    void cellCenter_fallsBackToSourceWhenNoWalkableNeighbor() {
        World world = TestWorlds.smallClear(3, 3);
        float[] center = DropPlacement.cellCenter(world, 1, 1);
        assertEquals(1.5f, center[0], 0.001f);
        assertEquals(1.5f, center[1], 0.001f);
    }
}
