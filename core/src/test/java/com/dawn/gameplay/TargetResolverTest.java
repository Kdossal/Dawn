package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.entity.EntityRegistry;
import com.dawn.item.ItemStack;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TargetResolverTest {
    private World world;
    private Entity player;

    @BeforeEach
    void setUp() {
        world = TestWorlds.smallClear(16, 16);
        player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
    }

    @Test
    void inReach_mouseCellReturned() {
        world.setObject(6, 4, BlockId.ROCK);
        TargetResolver.TargetCell cell =
                TargetResolver.resolve(
                        world,
                        player.def(),
                        player.getX(),
                        player.getY(),
                        6,
                        4,
                        ItemStack.empty());
        assertNotNull(cell);
        assertEquals(6, cell.x());
        assertEquals(4, cell.y());
        assertEquals(false, cell.clamped());
    }

    @Test
    void beyondReach_returnsNull_notReachEdgeCell() {
        world.setObject(7, 4, BlockId.ROCK);
        TargetResolver.TargetCell cell =
                TargetResolver.resolve(
                        world,
                        player.def(),
                        player.getX(),
                        player.getY(),
                        12,
                        4,
                        ItemStack.of(com.dawn.item.ItemId.HAMMER));
        assertNull(cell);
    }

    @Test
    void outOfBounds_returnsNull() {
        assertNull(
                TargetResolver.resolve(
                        world,
                        EntityRegistry.get(EntityId.PLAYER),
                        4f,
                        4f,
                        -1,
                        4,
                        ItemStack.empty()));
    }
}
