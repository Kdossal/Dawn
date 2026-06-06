package com.dawn.item;

import com.dawn.entity.Entity;
import com.dawn.gameplay.InteractionRules;
import com.dawn.gameplay.placement.PlacementPreview;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.SurfaceRules;
import com.dawn.world.structure.StructureKind;

/** Single dispatch for {@link Placeable} validation, application, and preview. */
public final class PlaceableExecutor {
    private PlaceableExecutor() {}

    public static boolean canPlace(World world, Entity entity, Placeable placeable, int anchorX, int anchorY) {
        if (placeable instanceof Placeable.Ground ground) {
            return InteractionRules.canPlaceGround(world, anchorX, anchorY, ground.blockId());
        }
        if (placeable instanceof Placeable.Block block) {
            return canPlaceBlock(world, entity, anchorX, anchorY, block.blockId());
        }
        if (placeable instanceof Placeable.Structure structure) {
            return world.getStructures().canPlaceAt(world, structure.kind(), anchorX, anchorY, entity);
        }
        return false;
    }

    public static boolean apply(World world, Entity entity, Placeable placeable, int anchorX, int anchorY) {
        if (placeable instanceof Placeable.Ground ground) {
            world.setGround(anchorX, anchorY, ground.blockId());
            return true;
        }
        if (placeable instanceof Placeable.Block block) {
            world.setObject(anchorX, anchorY, block.blockId());
            return true;
        }
        if (placeable instanceof Placeable.Structure structure) {
            return world.getStructures().place(world, structure.kind(), anchorX, anchorY, entity);
        }
        return false;
    }

    public static PlacementPreview toPreview(Placeable placeable, int anchorX, int anchorY, boolean valid) {
        if (placeable instanceof Placeable.Ground) {
            return new PlacementPreview.FloorCell(anchorX, anchorY, valid);
        }
        if (placeable instanceof Placeable.Block block) {
            return new PlacementPreview.BlockSprite(anchorX, anchorY, block.blockId(), valid);
        }
        if (placeable instanceof Placeable.Structure structure) {
            return new PlacementPreview.StructureMask(anchorX, anchorY, structure.kind(), valid);
        }
        return null;
    }

    public static String placementError(Placeable placeable) {
        if (placeable instanceof Placeable.Ground) {
            return "Need empty pit";
        }
        if (placeable instanceof Placeable.Block) {
            return "Can't place there";
        }
        return "Can't place structure there";
    }

    private static boolean canPlaceBlock(World world, Entity entity, int x, int y, BlockId blockId) {
        if (!SurfaceRules.canPlaceObject(world, entity, x, y)) {
            return false;
        }
        return blockId != BlockId.AIR;
    }
}
