package com.dawn.world.block.autotile;

import com.dawn.world.block.BlockId;
import java.util.EnumMap;
import java.util.Map;

public final class AutotileRegistry {
    private static final Map<BlockId, AutotileFamily> FAMILIES = AutotileDefinitionsLoader.load();

    private AutotileRegistry() {}

    public static AutotileFamily familyFor(BlockId blockId) {
        if (blockId == null || blockId == BlockId.AIR) {
            return null;
        }
        return FAMILIES.get(blockId);
    }

    public static Iterable<AutotileFamily> allFamilies() {
        return FAMILIES.values();
    }
}
