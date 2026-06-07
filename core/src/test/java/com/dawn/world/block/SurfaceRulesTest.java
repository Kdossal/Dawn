package com.dawn.world.block;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import org.junit.jupiter.api.Test;

class SurfaceRulesTest {

    @Test
    void walkable_onGrassOverDirt() {
        World world = TestWorlds.smallClear(4, 4);
        TestWorlds.setSolidDirt(world, 2, 2);
        world.setFloor(2, 2, BlockId.GRASS);
        assertTrue(SurfaceRules.canWalk(world, 2, 2));
    }

    @Test
    void notWalkable_onPit() {
        World world = TestWorlds.smallClear(4, 4);
        assertFalse(SurfaceRules.canWalk(world, 2, 2));
    }

    @Test
    void notWalkable_onWater() {
        World world = TestWorlds.smallClear(4, 4);
        world.setGround(2, 2, BlockId.WATER);
        assertFalse(SurfaceRules.canWalk(world, 2, 2));
    }

    @Test
    void placeGround_onPitOnly() {
        World world = TestWorlds.smallClear(4, 4);
        assertTrue(SurfaceRules.canPlaceGround(world, 2, 2, BlockId.DIRT));
        TestWorlds.setSolidDirt(world, 2, 2);
        assertFalse(SurfaceRules.canPlaceGround(world, 2, 2, BlockId.DIRT));
    }

    @Test
    void placeGrass_onDirtGroundOnly() {
        World world = TestWorlds.smallClear(4, 4);
        TestWorlds.setSolidDirt(world, 2, 2);
        assertTrue(SurfaceRules.canPlaceFloor(world, 2, 2, BlockId.GRASS));
        world.setGround(2, 2, BlockId.SAND);
        assertFalse(SurfaceRules.canPlaceFloor(world, 2, 2, BlockId.GRASS));
    }

    @Test
    void placeObject_passThroughAllowedOnOccupiedCell() {
        World world = TestWorlds.smallWalkable(6, 6);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 2.5f, 2f);
        assertTrue(SurfaceRules.canPlaceObject(world, player, 2, 2, BlockId.BUSH));
    }

    @Test
    void placeObject_solidBlockedOnOccupiedCell() {
        World world = TestWorlds.smallWalkable(6, 6);
        Entity player = new EntityManager().spawn(EntityId.PLAYER, 2.5f, 2f);
        assertFalse(SurfaceRules.canPlaceObject(world, player, 2, 2, BlockId.CRATE));
    }
}
