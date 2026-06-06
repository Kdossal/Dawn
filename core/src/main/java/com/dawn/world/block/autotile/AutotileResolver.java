package com.dawn.world.block.autotile;

import com.dawn.config.GameConfig;
import com.dawn.world.World;

/** Resolves world cell coordinates to an autotile grid cell. */
public final class AutotileResolver {
    private AutotileResolver() {}

    public static AutotileCell resolve(World world, int x, int y, AutotileFamily family) {
        return resolve(world, x, y, family, GameConfig.get().visualDecorSeed);
    }

    public static AutotileCell resolve(World world, int x, int y, AutotileFamily family, int seed) {
        int mask = CardinalMask.compute(world, x, y, family.neighborLayer(), family.neighborBlockId());
        if (mask == AutotileFamily.FULL_SURROUND_MASK) {
            return family.tileForFullSurround(x, y, seed);
        }
        return family.tileForMask(mask);
    }

    /** Deterministic non-negative hash for decor variant selection. */
    public static int positiveHash(int x, int y, int seed) {
        int h = x * 374761393 + y * 668265263 + seed * 1013904223;
        h = (h ^ (h >>> 16)) * 0x85ebca6b;
        h = (h ^ (h >>> 13)) * 0xc2b2ae35;
        return h & 0x7fffffff;
    }
}
