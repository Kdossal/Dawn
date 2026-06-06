package com.dawn.entity;

import com.dawn.world.World;
import com.dawn.world.block.SurfaceRules;

/** Floor/ground/object walkability used by {@link com.dawn.world.World#isSolidForMovement}. */
public final class EntityMovement {
    private EntityMovement() {}

    public static boolean canWalkCell(World world, int cellX, int cellY) {
        return SurfaceRules.canWalk(world, cellX, cellY);
    }
}
