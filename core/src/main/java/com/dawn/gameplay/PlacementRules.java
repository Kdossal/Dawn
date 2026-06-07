package com.dawn.gameplay;

import com.dawn.entity.Entity;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.item.PlaceableExecutor;
import com.dawn.world.World;

/** Validates placement for held placeable items. */
public final class PlacementRules {
    public record Result(boolean valid, Placeable placeable, int anchorX, int anchorY, String failureMessage) {}

    private PlacementRules() {}

    public static Result evaluate(
            World world, Entity entity, float playerX, float playerY, ItemStack held, int cellX, int cellY) {
        ItemDef def = ItemRegistry.get(held);
        if (def == null || def.placeable() == null) {
            return null;
        }
        Placeable placeable = def.placeable();
        float reach = ReachResolver.radiusForHeld(held);
        if (!InteractionRules.canTargetCell(world, entity, playerX, playerY, cellX, cellY, reach)) {
            return new Result(false, placeable, cellX, cellY, null);
        }
        boolean valid = PlaceableExecutor.canPlace(world, entity, placeable, cellX, cellY);
        String failure = valid ? null : placementFailureMessage(entity, placeable, cellX, cellY);
        return new Result(valid, placeable, cellX, cellY, failure);
    }

    private static String placementFailureMessage(
            Entity entity, Placeable placeable, int cellX, int cellY) {
        if (entity != null
                && InteractionRules.isEntityCell(entity, cellX, cellY)
                && placeable instanceof Placeable.Block) {
            return "Can't place that on yourself";
        }
        return PlaceableExecutor.placementError(placeable);
    }
}
