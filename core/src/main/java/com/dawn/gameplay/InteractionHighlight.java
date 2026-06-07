package com.dawn.gameplay;

import com.dawn.entity.Entity;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.item.ItemStack;
import com.dawn.world.World;
import com.dawn.world.structure.StructureInstance;
import com.dawn.world.structure.StructureKind;
import java.util.List;

/** Hover highlights for breakable targets the player can interact with using the held item. */
public final class InteractionHighlight {
    public sealed interface Highlight {
        record BlockSprite(int cellX, int cellY, com.dawn.world.block.Layer layer, com.dawn.world.block.BlockId blockId)
                implements Highlight {}

        record StructureMask(int anchorX, int anchorY, StructureKind kind) implements Highlight {}
    }

    private InteractionHighlight() {}

    public static List<Highlight> resolve(
            World world, Entity entity, float playerX, float playerY, ItemStack held, TargetCell hover) {
        if (hover == null) {
            return List.of();
        }
        float reach = ReachResolver.radiusForHeld(held);
        if (!InteractionRules.canTargetCell(world, entity, playerX, playerY, hover.x(), hover.y(), reach)) {
            return List.of();
        }
        BreakTarget breakTarget =
                InteractionRules.resolveToolBreak(world, held, hover.x(), hover.y(), entity);
        if (breakTarget == null) {
            return List.of();
        }

        StructureInstance structure = world.getStructures().getAt(hover.x(), hover.y());
        if (structure != null) {
            return List.of(new Highlight.StructureMask(
                    structure.anchorX(), structure.anchorY(), structure.kind()));
        }
        return List.of(new Highlight.BlockSprite(
                breakTarget.x(), breakTarget.y(), breakTarget.layer(), breakTarget.blockId()));
    }
}
