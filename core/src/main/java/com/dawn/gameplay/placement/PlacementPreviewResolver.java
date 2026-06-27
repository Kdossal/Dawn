package com.dawn.gameplay.placement;

import com.dawn.entity.Entity;
import com.dawn.gameplay.PlacementRules;
import com.dawn.gameplay.ReachResolver;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.item.PlaceableExecutor;
import com.dawn.item.PlaceableResolver;
import com.dawn.world.World;
import java.util.List;

public final class PlacementPreviewResolver {
    private PlacementPreviewResolver() {}

    public static List<PlacementPreview> resolve(
            World world, Entity entity, float playerX, float playerY, ItemStack held, TargetCell hover) {
        if (hover == null) {
            return List.of();
        }
        ItemDef def = ItemRegistry.get(held);
        if (def == null || def.placeable() == null) {
            return List.of();
        }
        return resolve(
                world,
                entity,
                playerX,
                playerY,
                def.placeable(),
                ReachResolver.radiusCellsFloatForHeld(held),
                hover);
    }

    public static List<PlacementPreview> resolve(
            World world,
            Entity entity,
            float playerX,
            float playerY,
            Placeable placeable,
            float reach,
            TargetCell hover) {
        if (hover == null || placeable == null) {
            return List.of();
        }
        PlacementRules.Result result =
                PlacementRules.evaluate(world, entity, playerX, playerY, placeable, reach, hover.x(), hover.y());
        if (result == null) {
            return List.of();
        }
        var previewTarget = PlaceableResolver.resolve(world, entity, placeable, hover.x(), hover.y());
        if (previewTarget == null) {
            return List.of();
        }
        PlacementPreview preview =
                PlaceableExecutor.toPreview(
                        previewTarget, result.anchorX(), result.anchorY(), result.valid());
        return preview == null ? List.of() : List.of(preview);
    }
}
