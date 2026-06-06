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
            String message =
                    InteractionRules.isEntityCell(entity, cellX, cellY) ? "Can't place on yourself" : null;
            return new Result(false, placeable, cellX, cellY, message);
        }
        boolean valid = PlaceableExecutor.canPlace(world, entity, placeable, cellX, cellY);
        String failure = valid ? null : PlaceableExecutor.placementError(placeable);
        return new Result(valid, placeable, cellX, cellY, failure);
    }
}
