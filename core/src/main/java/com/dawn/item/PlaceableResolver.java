package com.dawn.item;

import com.dawn.entity.Entity;
import com.dawn.gameplay.InteractionRules;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.SurfaceRules;

/** Picks the effective placement variant for dual-mode items. */
public final class PlaceableResolver {
    private PlaceableResolver() {}

    /**
     * @return resolved {@link Placeable} when valid at {@code (x, y)}, or {@code null}
     */
    public static Placeable resolve(World world, Entity entity, Placeable placeable, int x, int y) {
        if (placeable instanceof Placeable.GroundOrObject dual) {
            if (InteractionRules.canPlaceGround(world, x, y, dual.groundBlockId())) {
                return new Placeable.Ground(dual.groundBlockId());
            }
            if (dual.objectBlockId() != BlockId.AIR
                    && SurfaceRules.canPlaceObject(world, entity, x, y, dual.objectBlockId())) {
                return new Placeable.Block(dual.objectBlockId());
            }
            return null;
        }
        if (placeable instanceof Placeable.Ground ground) {
            return InteractionRules.canPlaceGround(world, x, y, ground.blockId()) ? placeable : null;
        }
        if (placeable instanceof Placeable.Block block) {
            return SurfaceRules.canPlaceObject(world, entity, x, y, block.blockId()) ? placeable : null;
        }
        if (placeable instanceof Placeable.Structure structure) {
            return world.getStructures().canPlaceAt(world, structure.kind(), x, y, entity) ? placeable : null;
        }
        return null;
    }
}
