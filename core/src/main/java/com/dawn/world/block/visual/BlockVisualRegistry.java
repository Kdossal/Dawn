package com.dawn.world.block.visual;

import com.dawn.world.block.BlockId;
import java.util.EnumMap;
import java.util.Map;

public final class BlockVisualRegistry {
    private static final Map<BlockId, BlockVisualDef> DEFS = new EnumMap<>(BlockId.class);
    private static final int[] MAX_CULL_PAD = new int[4];

    static {
        DEFS.putAll(BlockVisualDefinitionsLoader.load());
        recomputeMaxCullPad();
    }

    private BlockVisualRegistry() {}

    private static void recomputeMaxCullPad() {
        MAX_CULL_PAD[0] = 0;
        MAX_CULL_PAD[1] = 0;
        MAX_CULL_PAD[2] = 0;
        MAX_CULL_PAD[3] = 0;
        for (BlockVisualDef def : DEFS.values()) {
            int[] pad = def.cullPaddingCells();
            MAX_CULL_PAD[0] = Math.max(MAX_CULL_PAD[0], pad[0]);
            MAX_CULL_PAD[1] = Math.max(MAX_CULL_PAD[1], pad[1]);
            MAX_CULL_PAD[2] = Math.max(MAX_CULL_PAD[2], pad[2]);
            MAX_CULL_PAD[3] = Math.max(MAX_CULL_PAD[3], pad[3]);
        }
    }

    public static BlockVisualDef get(BlockId id) {
        if (id == null || id == BlockId.AIR) {
            return null;
        }
        BlockVisualDef def = DEFS.get(id);
        if (def != null) {
            return def;
        }
        return null;
    }

    /** True when {@link #get} returns a concrete def (everything that maps to sprites should). */
    public static boolean hasVisual(BlockId id) {
        return id != null && id != BlockId.AIR && DEFS.containsKey(id);
    }

    /** {@code [padLeft, padRight, padDown, padUp]} in cells for {@link com.dawn.world.render.WorldRenderer#visibleCellBounds}. */
    public static int[] maxCullPaddingCells() {
        return MAX_CULL_PAD.clone();
    }
}
