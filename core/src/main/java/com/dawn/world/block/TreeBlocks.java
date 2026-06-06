package com.dawn.world.block;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

/** Oak and spruce trunk/stump pairs and break-chain helpers. */
public final class TreeBlocks {
    private static final EnumMap<BlockId, BlockId> TRUNK_TO_STUMP = new EnumMap<>(BlockId.class);
    private static final Set<BlockId> TRUNKS = EnumSet.noneOf(BlockId.class);
    private static final Set<BlockId> STUMPS = EnumSet.noneOf(BlockId.class);

    static {
        pair(BlockId.OAK_TREE, BlockId.OAK_STUMP);
        pair(BlockId.SPRUCE_TREE, BlockId.SPRUCE_STUMP);
    }

    private TreeBlocks() {}

    private static void pair(BlockId trunk, BlockId stump) {
        TRUNK_TO_STUMP.put(trunk, stump);
        TRUNKS.add(trunk);
        STUMPS.add(stump);
    }

    public static boolean isTrunk(BlockId id) {
        return TRUNKS.contains(id);
    }

    public static boolean isStump(BlockId id) {
        return STUMPS.contains(id);
    }

    /** Stump left after chopping a trunk, or null if not a trunk. */
    public static BlockId stumpFor(BlockId trunk) {
        return TRUNK_TO_STUMP.get(trunk);
    }
}
