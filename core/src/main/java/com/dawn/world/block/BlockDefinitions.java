package com.dawn.world.block;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Block metadata registry (layer, movement, interaction, mining health). */
public final class BlockDefinitions {
    public record BlockDef(
            BlockId id,
            Layer layer,
            GroundKind groundKind,
            boolean walkableFloor,
            boolean passThroughObject,
            boolean breakable,
            Set<InteractionTag> breakTags,
            float breakHealth,
            boolean fadeWhenPlayerBehind,
            boolean triggersOcclusionFade) {

        public InteractionTag primaryBreakTag() {
            if (breakTags.contains(InteractionTag.NONE)) {
                return InteractionTag.NONE;
            }
            if (breakTags.contains(InteractionTag.MINE)) {
                return InteractionTag.MINE;
            }
            if (breakTags.contains(InteractionTag.CHOP)) {
                return InteractionTag.CHOP;
            }
            if (breakTags.contains(InteractionTag.DIG)) {
                return InteractionTag.DIG;
            }
            return breakTags.isEmpty() ? null : breakTags.iterator().next();
        }
    }

    private static final Map<BlockId, BlockDef> DEFS = new EnumMap<>(BlockId.class);

    static {
        def(BlockId.AIR, Layer.FLOOR, GroundKind.NONE, false, true, false, Set.of(), 0f);
        def(BlockId.PIT, Layer.GROUND, GroundKind.PIT, false, false, false, Set.of(), 0f);
        def(BlockId.DIRT, Layer.GROUND, GroundKind.SOLID, false, false, true, Set.of(InteractionTag.DIG), 14f);
        def(BlockId.SAND, Layer.GROUND, GroundKind.SOLID, false, false, true, Set.of(InteractionTag.DIG), 12f);
        def(BlockId.STONE, Layer.GROUND, GroundKind.SOLID, false, false, true, Set.of(InteractionTag.DIG), 55f);
        def(BlockId.WATER, Layer.GROUND, GroundKind.WATER, false, false, true, Set.of(InteractionTag.DIG), 8f);
        def(BlockId.GRASS, Layer.FLOOR, GroundKind.NONE, true, false, true, Set.of(InteractionTag.DIG), 10f);
        def(BlockId.ROCK, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.MINE), 55f);
        def(BlockId.BUSH, Layer.OBJECT, GroundKind.NONE, false, true, true, Set.of(InteractionTag.NONE), 8f, true, true);
        def(BlockId.OAK_TREE, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.CHOP), 22f, true, true);
        def(BlockId.OAK_STUMP, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.CHOP), 45f, true, false);
        def(BlockId.SPRUCE_TREE, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.CHOP), 22f, true, true);
        def(BlockId.SPRUCE_STUMP, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.CHOP), 45f, true, false);
        def(
                BlockId.CRATE,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                false,
                true,
                EnumSet.of(InteractionTag.MINE, InteractionTag.CHOP),
                20f);
        def(
                BlockId.BED_FOOT,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                false,
                true,
                EnumSet.of(InteractionTag.MINE, InteractionTag.CHOP),
                20f);
        def(
                BlockId.BED_HEAD,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                false,
                true,
                EnumSet.of(InteractionTag.MINE, InteractionTag.CHOP),
                20f);
    }

    private BlockDefinitions() {}

    private static void def(
            BlockId id,
            Layer layer,
            GroundKind groundKind,
            boolean walkableFloor,
            boolean passThroughObject,
            boolean breakable,
            Set<InteractionTag> breakTags,
            float breakHealth) {
        def(id, layer, groundKind, walkableFloor, passThroughObject, breakable, breakTags, breakHealth, false, false);
    }

    private static void def(
            BlockId id,
            Layer layer,
            GroundKind groundKind,
            boolean walkableFloor,
            boolean passThroughObject,
            boolean breakable,
            Set<InteractionTag> breakTags,
            float breakHealth,
            boolean fadeWhenPlayerBehind,
            boolean triggersOcclusionFade) {
        DEFS.put(
                id,
                new BlockDef(
                        id,
                        layer,
                        groundKind,
                        walkableFloor,
                        passThroughObject,
                        breakable,
                        breakTags,
                        breakHealth,
                        fadeWhenPlayerBehind,
                        triggersOcclusionFade));
    }

    public static BlockDef get(BlockId id) {
        return DEFS.get(id);
    }

    public static GroundKind groundKind(BlockId id) {
        BlockDef d = get(id);
        return d == null ? GroundKind.NONE : d.groundKind();
    }

    public static float breakHealth(BlockId id) {
        BlockDef d = get(id);
        return d == null ? 0f : d.breakHealth();
    }

    public static boolean canWalkFloor(BlockId id) {
        BlockDef d = get(id);
        return d != null && d.walkableFloor();
    }

    public static boolean isPassThroughObject(BlockId id) {
        if (id == BlockId.AIR) {
            return true;
        }
        BlockDef d = get(id);
        return d != null && d.passThroughObject();
    }

    /** @deprecated use {@link #isPassThroughObject} */
    @Deprecated
    public static boolean isPassThroughBlock(BlockId id) {
        return isPassThroughObject(id);
    }

    public static boolean fadeWhenPlayerBehind(BlockId id) {
        BlockDef d = get(id);
        return d != null && d.fadeWhenPlayerBehind();
    }

    public static boolean triggersOcclusionFade(BlockId id) {
        BlockDef d = get(id);
        return d != null && d.triggersOcclusionFade();
    }
}
