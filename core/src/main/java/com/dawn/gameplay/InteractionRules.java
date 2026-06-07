package com.dawn.gameplay;

import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.entity.Entity;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockDefinitions.BlockDef;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.InteractionTag;
import com.dawn.world.block.Layer;
import com.dawn.world.block.SurfaceRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class InteractionRules {
    private InteractionRules() {}

    public static boolean isEntityCell(Entity entity, int x, int y) {
        return entity.occupiesCell(x, y);
    }

    public static boolean canTargetCell(
            World world, Entity entity, float entityX, float entityY, int x, int y, float reachRadius) {
        if (!world.inBounds(x, y)) {
            return false;
        }
        return ReachResolver.inReach(entity.def(), entityX, entityY, x, y, reachRadius);
    }

    /** True when breaking this target on a cell the entity occupies would not trap the entity. */
    public static boolean canBreakOnOccupiedCell(BreakTarget target) {
        return switch (target.layer()) {
            case GROUND -> false;
            case FLOOR, OBJECT -> true;
        };
    }

    public static boolean canBreakOnOccupiedCell(World world, Entity entity, int x, int y) {
        BreakTarget target = inspectBreak(world, x, y);
        return target != null && (!isEntityCell(entity, x, y) || canBreakOnOccupiedCell(target));
    }

    public static BreakTarget inspectBreak(World world, int x, int y) {
        BreakTarget structureTarget = world.getStructures().resolveBreakTarget(world, x, y);
        if (structureTarget != null) {
            return structureTarget;
        }
        Layer layer = world.getPrimaryInteractLayer(x, y);
        if (layer == null) {
            return null;
        }
        BlockId id = world.getBlockAtLayer(x, y, layer);
        BlockDef def = BlockDefinitions.get(id);
        if (def == null || !def.breakable()) {
            return null;
        }
        return new BreakTarget(x, y, layer, id, def.breakHealth());
    }

    /** Break target at {@code (x,y)} only if {@code held} is a matching tool (or hands for NONE-tagged blocks). */
    public static BreakTarget resolveToolBreak(World world, ItemStack held, int x, int y) {
        return resolveToolBreak(world, held, x, y, null);
    }

    public static BreakTarget resolveToolBreak(World world, ItemStack held, int x, int y, Entity entity) {
        BreakTarget target = inspectBreak(world, x, y);
        if (target == null) {
            return null;
        }
        if (entity != null && isEntityCell(entity, x, y) && !canBreakOnOccupiedCell(target)) {
            return null;
        }
        BlockDef def = BlockDefinitions.get(target.blockId());
        if (def == null || toolError(held, def.breakTags()) != null) {
            return null;
        }
        return target;
    }

    public static String toolError(ItemStack held, Set<InteractionTag> requiredTags) {
        if (requiredTags == null || requiredTags.isEmpty() || requiredTags.contains(InteractionTag.NONE)) {
            return null;
        }
        ItemDef toolDef = held == null || held.isEmpty() ? null : ItemRegistry.get(held);
        if (toolDef == null) {
            return needToolMessage(requiredTags);
        }
        for (InteractionTag tag : requiredTags) {
            if (tag != InteractionTag.NONE && toolDef.matches(tag)) {
                return null;
            }
        }
        return needToolMessage(requiredTags);
    }

    private static String needToolMessage(Set<InteractionTag> tags) {
        List<String> names = new ArrayList<>();
        for (InteractionTag tag : tags) {
            if (tag != InteractionTag.NONE) {
                names.add(toolDisplayName(tag));
            }
        }
        if (names.isEmpty()) {
            return "Need a tool";
        }
        if (names.size() == 1) {
            return "Need " + names.get(0);
        }
        return "Need " + String.join(" or ", names);
    }

    private static String toolDisplayName(InteractionTag tag) {
        return switch (tag) {
            case MINE -> "pickaxe";
            case CHOP -> "axe";
            case DIG -> "shovel";
            default -> tag.name().toLowerCase();
        };
    }

    public static boolean canPlaceGround(World world, int x, int y, BlockId groundToPlace) {
        return SurfaceRules.canPlaceGround(world, x, y, groundToPlace);
    }
}
