package com.dawn.world.block;

import com.dawn.world.World;
import java.util.EnumMap;
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
            boolean triggersOcclusionFade,
            /** Fraction of light that passes through (0 = fully opaque, 1 = fully transparent). */
            float lightTransmission,
            float lightEmission,
            int lightRadius,
            float lightColorR,
            float lightColorG,
            float lightColorB) {

        public InteractionTag primaryBreakTag() {
            if (breakTags.contains(InteractionTag.NONE)) {
                return InteractionTag.NONE;
            }
            if (breakTags.contains(InteractionTag.BREAK)) {
                return InteractionTag.BREAK;
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
        def(BlockId.DIRT_GROUND, Layer.GROUND, GroundKind.SOLID, false, false, true, Set.of(InteractionTag.DIG), 14f);
        def(BlockId.SAND_GROUND, Layer.GROUND, GroundKind.SOLID, false, false, true, Set.of(InteractionTag.DIG), 12f);
        def(BlockId.STONE_GROUND, Layer.GROUND, GroundKind.SOLID, false, false, true, Set.of(InteractionTag.DIG), 55f);
        def(BlockId.WATER, Layer.GROUND, GroundKind.WATER, false, false, true, Set.of(InteractionTag.DIG), 8f);
        def(BlockId.GRASS, Layer.FLOOR, GroundKind.NONE, true, false, true, Set.of(InteractionTag.DIG), 10f);
        def(BlockId.ROCK, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.BREAK), 55f);
        def(BlockId.BUSH, Layer.OBJECT, GroundKind.NONE, false, true, true, Set.of(InteractionTag.NONE), 8f, true, true, 0.15f);
        def(BlockId.OAK_TREE, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.CHOP), 22f, true, true, 0.15f);
        def(BlockId.OAK_STUMP, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.DIG), 45f, true, false, 0.15f);
        def(BlockId.SPRUCE_TREE, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.CHOP), 22f, true, true, 0.15f);
        def(BlockId.SPRUCE_STUMP, Layer.OBJECT, GroundKind.NONE, false, false, true, Set.of(InteractionTag.DIG), 45f, true, false, 0.15f);
        def(
                BlockId.CRATE,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                false,
                true,
                Set.of(InteractionTag.BREAK),
                20f,
                false,
                false,
                0.5f,
                0f,
                0);
        def(
                BlockId.BED_FOOT,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                false,
                true,
                Set.of(InteractionTag.BREAK),
                20f);
        def(
                BlockId.BED_HEAD,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                false,
                true,
                Set.of(InteractionTag.BREAK),
                20f);
        def(
                BlockId.LANTERN,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                true,
                true,
                Set.of(InteractionTag.NONE),
                1f,
                false,
                false,
                1.0f,
                1f,
                28,
                1.0f,
                0.72f,
                0.50f);
        def(
                BlockId.CAMPFIRE,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                true,
                true,
                Set.of(InteractionTag.NONE),
                1f,
                false,
                false,
                1.0f,
                1f,
                28,
                1.0f,
                0.85f,
                0.35f);
        def(
                BlockId.STONE_WALL,
                Layer.OBJECT,
                GroundKind.NONE,
                false,
                false,
                true,
                Set.of(InteractionTag.BREAK),
                45f,
                false,
                false,
                0.1f,
                0f,
                0);
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
        def(
                id,
                layer,
                groundKind,
                walkableFloor,
                passThroughObject,
                breakable,
                breakTags,
                breakHealth,
                fadeWhenPlayerBehind,
                triggersOcclusionFade,
                1.0f,
                0f,
                0,
                1f,
                1f,
                1f);
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
            boolean triggersOcclusionFade,
            float lightTransmission) {
        def(
                id,
                layer,
                groundKind,
                walkableFloor,
                passThroughObject,
                breakable,
                breakTags,
                breakHealth,
                fadeWhenPlayerBehind,
                triggersOcclusionFade,
                lightTransmission,
                0f,
                0,
                1f,
                1f,
                1f);
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
            boolean triggersOcclusionFade,
            float lightTransmission,
            float lightEmission,
            int lightRadius) {
        def(
                id,
                layer,
                groundKind,
                walkableFloor,
                passThroughObject,
                breakable,
                breakTags,
                breakHealth,
                fadeWhenPlayerBehind,
                triggersOcclusionFade,
                lightTransmission,
                lightEmission,
                lightRadius,
                1f,
                1f,
                1f);
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
            boolean triggersOcclusionFade,
            float lightTransmission,
            float lightEmission,
            int lightRadius,
            float lightColorR,
            float lightColorG,
            float lightColorB) {
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
                        triggersOcclusionFade,
                        lightTransmission,
                        lightEmission,
                        lightRadius,
                        lightColorR,
                        lightColorG,
                        lightColorB));
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

    public static boolean fadeWhenPlayerBehind(BlockId id) {
        BlockDef d = get(id);
        return d != null && d.fadeWhenPlayerBehind();
    }

    public static boolean triggersOcclusionFade(BlockId id) {
        BlockDef d = get(id);
        return d != null && d.triggersOcclusionFade();
    }

    /** Fraction of light that passes through this block (0 = fully opaque, 1 = fully transparent). */
    public static float lightTransmission(BlockId id) {
        BlockDef d = get(id);
        return d == null ? 1.0f : d.lightTransmission();
    }

    public static float lightEmission(BlockId id) {
        BlockDef d = get(id);
        return d == null ? 0f : d.lightEmission();
    }

    public static int lightRadius(BlockId id) {
        BlockDef d = get(id);
        return d == null ? 0 : d.lightRadius();
    }

    public static float lightColorR(BlockId id) {
        BlockDef d = get(id);
        return d == null ? 1f : d.lightColorR();
    }

    public static float lightColorG(BlockId id) {
        BlockDef d = get(id);
        return d == null ? 1f : d.lightColorG();
    }

    public static float lightColorB(BlockId id) {
        BlockDef d = get(id);
        return d == null ? 1f : d.lightColorB();
    }

    public static float[] lightColor(BlockId id) {
        return new float[] {lightColorR(id), lightColorG(id), lightColorB(id)};
    }

    public static float[] lightColorAt(World world, int x, int y) {
        if (!world.inBounds(x, y)) {
            return new float[] {1f, 1f, 1f};
        }
        return lightColor(world.getObject(x, y));
    }

    /** Returns true when the cell is fully opaque (transmission == 0). OBJECT layer only for v1. */
    public static boolean isLightBlockerAt(World world, int x, int y) {
        if (!world.inBounds(x, y)) {
            return true;
        }
        return lightTransmission(world.getObject(x, y)) <= 0f;
    }

    public static float lightEmissionAt(World world, int x, int y) {
        if (!world.inBounds(x, y)) {
            return 0f;
        }
        return lightEmission(world.getObject(x, y));
    }
}
