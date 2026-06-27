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
import com.dawn.item.Placeable;
import com.dawn.item.PlaceableExecutor;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class PlacementRulesTest {
    @Test
    void lanternOnOpenGroundIsValid() {
        World world = TestWorlds.smallClear(8, 8);
        TestWorlds.setSolidDirt(world, 5, 4);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        PlacementRules.Result result =
                PlacementRules.evaluate(world, player, 4f, 4f, ItemStack.of(ItemId.LANTERN), 5, 4);
        assertNotNull(result);
        assertTrue(result.valid());
    }

    @Test
    void lanternBlockedWhenCellOccupied() {
        World world = TestWorlds.smallClear(8, 8);
        TestWorlds.setSolidDirt(world, 5, 4);
        world.setObject(5, 4, BlockId.ROCK);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        PlacementRules.Result result =
                PlacementRules.evaluate(world, player, 4f, 4f, ItemStack.of(ItemId.LANTERN), 5, 4);
        assertNotNull(result);
        assertFalse(result.valid());
    }

    @Test
    void saplingBlockedOnPlayerCell() {
        World world = TestWorlds.smallWalkable(8, 8);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 5.5f, 4f);
        PlacementRules.Result result =
                PlacementRules.evaluate(
                        world, player, player.getX(), player.getY(), ItemStack.of(ItemId.OAK_SAPLING), 5, 4);
        assertNotNull(result);
        assertFalse(result.valid());
        assertEquals("Can't place that on yourself", result.failureMessage());
    }

    @Test
    void saplingOnOpenGroundPlacesBlock() {
        World world = TestWorlds.smallClear(8, 8);
        TestWorlds.setSolidDirt(world, 5, 4);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        PlacementRules.Result result =
                PlacementRules.evaluate(world, player, 4f, 4f, ItemStack.of(ItemId.OAK_SAPLING), 5, 4);
        assertNotNull(result);
        assertTrue(result.valid());
        assertTrue(result.placeable() instanceof Placeable.Block block
                && block.blockId() == BlockId.OAK_TREE);
    }

    @Test
    void groundPlaceableOnPitIsValid() {
        World world = TestWorlds.smallClear(8, 8);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        assertTrue(PlaceableExecutor.canPlace(
                world, player, new Placeable.Ground(BlockId.STONE_GROUND), 5, 4));
    }
}
