package com.dawn.world.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import org.junit.jupiter.api.Test;

class BlockBreakEffectsTest {

    @Test
    void oakTreeBreak_leavesOakStump() {
        World world = TestWorlds.smallClear(8, 8);
        world.setObject(3, 4, BlockId.OAK_TREE);
        assertTrue(BlockBreakEffects.breakObjectLayer(world, 3, 4, BlockId.OAK_TREE).isPresent());
        assertEquals(BlockId.OAK_STUMP, world.getObject(3, 4));
    }

    @Test
    void spruceTreeBreak_leavesSpruceStump() {
        World world = TestWorlds.smallClear(8, 8);
        world.setObject(3, 4, BlockId.SPRUCE_TREE);
        assertTrue(BlockBreakEffects.breakObjectLayer(world, 3, 4, BlockId.SPRUCE_TREE).isPresent());
        assertEquals(BlockId.SPRUCE_STUMP, world.getObject(3, 4));
    }

    @Test
    void grassDig_clearsFloor_leavesGround() {
        World world = TestWorlds.smallClear(8, 8);
        TestWorlds.setSolidDirt(world, 3, 4);
        world.setFloor(3, 4, BlockId.GRASS);
        assertTrue(BlockBreakEffects.digFloor(world, 3, 4, BlockId.GRASS).isPresent());
        assertEquals(BlockId.AIR, world.getFloor(3, 4));
        assertEquals(BlockId.DIRT_GROUND, world.getGround(3, 4));
    }

    @Test
    void groundDig_clearsToPit() {
        World world = TestWorlds.smallClear(8, 8);
        TestWorlds.setSolidDirt(world, 3, 4);
        assertTrue(BlockBreakEffects.digGround(world, 3, 4, BlockId.DIRT_GROUND).isPresent());
        assertEquals(BlockId.PIT, world.getGround(3, 4));
    }

    @Test
    void oakStumpBreak_clearsObject() {
        World world = TestWorlds.smallClear(8, 8);
        world.setObject(3, 4, BlockId.OAK_STUMP);
        assertTrue(BlockBreakEffects.breakObjectLayer(world, 3, 4, BlockId.OAK_STUMP).isPresent());
        assertEquals(BlockId.AIR, world.getObject(3, 4));
    }
}
