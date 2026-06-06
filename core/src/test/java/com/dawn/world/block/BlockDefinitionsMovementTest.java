package com.dawn.world.block;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import org.junit.jupiter.api.Test;

class BlockDefinitionsMovementTest {

    @Test
    void treesAndStumps_blockMovement_bushDoesNot() {
        assertFalse(BlockDefinitions.isPassThroughBlock(BlockId.OAK_TREE));
        assertFalse(BlockDefinitions.isPassThroughBlock(BlockId.OAK_STUMP));
        assertFalse(BlockDefinitions.isPassThroughBlock(BlockId.SPRUCE_TREE));
        assertFalse(BlockDefinitions.isPassThroughBlock(BlockId.SPRUCE_STUMP));
        assertTrue(BlockDefinitions.isPassThroughBlock(BlockId.BUSH));
    }

    @Test
    void oakTreeCell_isSolidForMovement() {
        World world = TestWorlds.smallWalkable(8, 8);
        world.setObject(3, 4, BlockId.OAK_TREE);
        assertTrue(world.isSolidForMovement(3, 4));
    }

    @Test
    void spruceStumpCell_isSolidForMovement() {
        World world = TestWorlds.smallWalkable(8, 8);
        world.setObject(3, 4, BlockId.SPRUCE_STUMP);
        assertTrue(world.isSolidForMovement(3, 4));
    }
}
