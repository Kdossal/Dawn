package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class PlacementRulesTest {
    @Test
    void dirtOnEmptyPitIsValid() {
        World world = TestWorlds.smallClear(8, 8);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        PlacementRules.Result result =
                PlacementRules.evaluate(world, player, 4f, 4f, ItemStack.of(ItemId.DIRT_CLUMP), 5, 4);
        assertNotNull(result);
        assertTrue(result.valid());
    }

    @Test
    void crateBlockedWhenCellOccupied() {
        World world = TestWorlds.smallClear(8, 8);
        TestWorlds.setSolidDirt(world, 5, 4);
        world.setObject(5, 4, BlockId.ROCK);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        PlacementRules.Result result =
                PlacementRules.evaluate(world, player, 4f, 4f, ItemStack.of(ItemId.CRATE), 5, 4);
        assertNotNull(result);
        assertFalse(result.valid());
    }

    @Test
    void crateBlockedOnPlayerCell() {
        World world = TestWorlds.smallWalkable(8, 8);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 5.5f, 4f);
        PlacementRules.Result result =
                PlacementRules.evaluate(
                        world, player, player.getX(), player.getY(), ItemStack.of(ItemId.CRATE), 5, 4);
        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals("Can't place that on yourself", result.failureMessage());
    }
}
