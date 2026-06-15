package com.dawn.gameplay;

import com.dawn.entity.Entity;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockDefinitions.BlockDef;
import com.dawn.world.block.InteractionTag;
import java.util.Set;

/** Resolves LMB/RMB hint verbs from held item and hover cell (read-only; mirrors InteractionRules). */
public final class ClickHintResolver {
    private ClickHintResolver() {}

    public static ClickHints resolve(World world, Entity entity, ItemStack held, TargetCell hover) {
        ClickVerb left = resolveLeft(world, entity, held, hover);
        ClickVerb right = resolveRight(entity, held);
        return new ClickHints(left, right);
    }

    private static ClickVerb resolveRight(Entity entity, ItemStack held) {
        if (EatSystem.canEat(entity, held)) {
            return ClickVerb.EAT;
        }
        ItemDef def = ItemRegistry.get(held);
        if (def != null && def.isEdible()) {
            return null;
        }
        if (def != null && def.canPlace()) {
            return ClickVerb.PLACE;
        }
        return null;
    }

    private static ClickVerb resolveLeft(World world, Entity entity, ItemStack held, TargetCell hover) {
        if (hover == null) {
            return ClickVerb.ATTACK;
        }
        float reach = ReachResolver.radiusCellsFloatForHeld(held);
        if (!InteractionRules.canTargetCell(
                world, entity, entity.getX(), entity.getY(), hover.x(), hover.y(), reach)) {
            return ClickVerb.ATTACK;
        }

        BreakTarget breakTarget =
                InteractionRules.resolveToolBreak(world, held, hover.x(), hover.y(), entity);
        if (breakTarget != null) {
            return verbForBreakTarget(breakTarget);
        }

        BreakTarget inspected = InteractionRules.inspectBreak(world, hover.x(), hover.y());
        if (inspected != null && isHandGrabBlock(inspected)) {
            return ClickVerb.GRAB;
        }
        return ClickVerb.ATTACK;
    }

    private static boolean isHandGrabBlock(BreakTarget target) {
        BlockDef def = BlockDefinitions.get(target.blockId());
        if (def == null) {
            return false;
        }
        Set<InteractionTag> tags = def.breakTags();
        return tags != null && tags.contains(InteractionTag.NONE);
    }

    private static ClickVerb verbForBreakTarget(BreakTarget target) {
        BlockDef def = BlockDefinitions.get(target.blockId());
        if (def == null) {
            return ClickVerb.ATTACK;
        }
        InteractionTag tag = def.primaryBreakTag();
        if (tag == null) {
            return ClickVerb.ATTACK;
        }
        return switch (tag) {
            case DIG -> ClickVerb.DIG;
            case MINE -> ClickVerb.MINE;
            case CHOP -> ClickVerb.CHOP;
            case NONE -> ClickVerb.GRAB;
        };
    }
}
