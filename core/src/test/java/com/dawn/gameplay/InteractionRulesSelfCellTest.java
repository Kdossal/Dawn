package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InteractionRulesSelfCellTest {
    private World world;
    private Entity player;

    @BeforeEach
    void setUp() {
        world = TestWorlds.smallWalkable(8, 8);
        player = new EntityManager().spawn(EntityId.PLAYER, 5.5f, 4f);
    }

    @Test
    void resolveToolBreak_blocksDiggingGroundUnderSelf() {
        world.setGround(5, 4, BlockId.DIRT_GROUND);
        world.setFloor(5, 4, BlockId.AIR);
        BreakTarget target =
                InteractionRules.resolveToolBreak(
                        world, ItemStack.of(ItemId.SHOVEL), 5, 4, player);
        assertNull(target);
    }

    @Test
    void resolveToolBreak_allowsDiggingGrassFloorOnSelf() {
        world.setFloor(5, 4, BlockId.GRASS);
        BreakTarget target =
                InteractionRules.resolveToolBreak(
                        world, ItemStack.of(ItemId.SHOVEL), 5, 4, player);
        assertNotNull(target);
        assertEquals(Layer.FLOOR, target.layer());
    }

    @Test
    void resolveToolBreak_allowsGrabbingBushOnSelf() {
        world.setObject(5, 4, BlockId.BUSH);
        BreakTarget target =
                InteractionRules.resolveToolBreak(world, ItemStack.empty(), 5, 4, player);
        assertNotNull(target);
        assertEquals(BlockId.BUSH, target.blockId());
    }

    @Test
    void canBreakOnOccupiedCell_groundIsBlocked() {
        world.setGround(5, 4, BlockId.DIRT_GROUND);
        world.setFloor(5, 4, BlockId.AIR);
        assertFalse(InteractionRules.canBreakOnOccupiedCell(world, player, 5, 4));
    }
}
