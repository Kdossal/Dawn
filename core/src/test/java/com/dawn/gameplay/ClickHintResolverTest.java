package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClickHintResolverTest {
    private World world;
    private Entity player;

    @BeforeEach
    void setUp() {
        world = TestWorlds.smallWalkable(12, 12);
        player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
    }

    @Test
    void emptyHand_noHover_attackOnly() {
        ClickHints hints = ClickHintResolver.resolve(world, player, ItemStack.empty(), null);
        assertEquals(ClickVerb.ATTACK, hints.left());
        assertNull(hints.rightOrNull());
    }

    @Test
    void emptyHand_bush_grab() {
        world.setObject(5, 4, BlockId.BUSH);
        ClickHints hints =
                ClickHintResolver.resolve(world, player, ItemStack.empty(), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.GRAB, hints.left());
    }

    @Test
    void emptyHand_stone_attack() {
        world.setObject(5, 4, BlockId.ROCK);
        ClickHints hints =
                ClickHintResolver.resolve(world, player, ItemStack.empty(), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.ATTACK, hints.left());
    }

    @Test
    void pickaxe_stone_mine() {
        world.setObject(5, 4, BlockId.ROCK);
        ClickHints hints =
                ClickHintResolver.resolve(
                        world, player, ItemStack.of(ItemId.PICKAXE), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.MINE, hints.left());
    }

    @Test
    void pickaxe_grass_attack() {
        world.setFloor(5, 4, BlockId.GRASS); // dirt ground from setUp
        ClickHints hints =
                ClickHintResolver.resolve(
                        world, player, ItemStack.of(ItemId.PICKAXE), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.ATTACK, hints.left());
    }

    @Test
    void axe_tree_chop() {
        world.setObject(5, 4, BlockId.OAK_TREE);
        ClickHints hints =
                ClickHintResolver.resolve(world, player, ItemStack.of(ItemId.AXE), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.CHOP, hints.left());
    }

    @Test
    void axe_stone_attack() {
        world.setObject(5, 4, BlockId.ROCK);
        ClickHints hints =
                ClickHintResolver.resolve(world, player, ItemStack.of(ItemId.AXE), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.ATTACK, hints.left());
    }

    @Test
    void shovel_dirt_dig() {
        world.setFloor(5, 4, BlockId.DIRT);
        ClickHints hints =
                ClickHintResolver.resolve(
                        world, player, ItemStack.of(ItemId.SHOVEL), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.DIG, hints.left());
    }

    @Test
    void shovel_stone_attack() {
        world.setObject(5, 4, BlockId.ROCK);
        ClickHints hints =
                ClickHintResolver.resolve(
                        world, player, ItemStack.of(ItemId.SHOVEL), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.ATTACK, hints.left());
    }

    @Test
    void placeable_showsPlaceOnRight() {
        ClickHints hints =
                ClickHintResolver.resolve(
                        world, player, ItemStack.of(ItemId.DIRT_CLUMP), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.ATTACK, hints.left());
        assertEquals(ClickVerb.PLACE, hints.rightOrNull());
    }

    @Test
    void placeable_onDirtGround_leftDig() {
        world.setGround(5, 4, BlockId.DIRT);
        world.setFloor(5, 4, BlockId.AIR);
        ClickHints hints =
                ClickHintResolver.resolve(
                        world, player, ItemStack.of(ItemId.SHOVEL), new TargetCell(5, 4, false));
        assertEquals(ClickVerb.DIG, hints.left());
        assertNull(hints.rightOrNull());
    }
}
