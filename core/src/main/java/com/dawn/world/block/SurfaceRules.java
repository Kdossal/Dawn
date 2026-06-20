package com.dawn.world.block;

import com.dawn.entity.Entity;
import com.dawn.world.World;

/** Walk and placement rules for ground / floor / object layers. */
public final class SurfaceRules {
    private SurfaceRules() {}

    public static boolean isSolidGround(BlockId ground) {
        return BlockDefinitions.groundKind(ground) == GroundKind.SOLID;
    }

    public static boolean canWalk(World world, int x, int y) {
        if (!world.inBounds(x, y)) {
            return false;
        }
        BlockId ground = world.getGround(x, y);
        if (BlockDefinitions.groundKind(ground) != GroundKind.SOLID) {
            return false;
        }
        BlockId floor = world.getFloor(x, y);
        if (floor != BlockId.AIR && !BlockDefinitions.canWalkFloor(floor)) {
            return false;
        }
        return BlockDefinitions.isPassThroughObject(world.getObject(x, y));
    }

    public static boolean canPlaceGround(World world, int x, int y, BlockId groundToPlace) {
        if (!world.inBounds(x, y)) {
            return false;
        }
        if (world.getGround(x, y) != BlockId.PIT) {
            return false;
        }
        if (world.getObject(x, y) != BlockId.AIR) {
            return false;
        }
        return BlockDefinitions.groundKind(groundToPlace) == GroundKind.SOLID;
    }

    public static boolean canPlaceFloor(World world, int x, int y, BlockId floorToPlace) {
        if (!world.inBounds(x, y)) {
            return false;
        }
        if (world.getObject(x, y) != BlockId.AIR) {
            return false;
        }
        if (world.getFloor(x, y) != BlockId.AIR) {
            return false;
        }
        if (floorToPlace == BlockId.GRASS) {
            return world.getGround(x, y) == BlockId.DIRT_GROUND;
        }
        return false;
    }

    public static boolean canPlaceObject(World world, Entity entity, int x, int y) {
        return canPlaceObject(world, entity, x, y, BlockId.AIR);
    }

    public static boolean canPlaceObject(World world, Entity entity, int x, int y, BlockId objectToPlace) {
        if (!world.inBounds(x, y)) {
            return false;
        }
        if (!isSolidGround(world.getGround(x, y))) {
            return false;
        }
        if (world.getObject(x, y) != BlockId.AIR) {
            return false;
        }
        if (entity != null && entity.occupiesCell(x, y) && !BlockDefinitions.isPassThroughObject(objectToPlace)) {
            return false;
        }
        return world.getStructures().getAt(x, y) == null;
    }
}
