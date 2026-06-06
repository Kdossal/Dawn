package com.dawn.world.block.autotile;

import com.dawn.assets.BlockTextureId;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;

/** Autotile sheet definition: cardinal mask lookup plus optional full-surround center variants. */
public record AutotileFamily(
        String id,
        BlockId blockId,
        BlockTextureId texture,
        int cols,
        int rows,
        int tileSizePx,
        Layer neighborLayer,
        BlockId neighborBlockId,
        AutotileCell[] maskTiles,
        AutotileCell[] centerTiles) {

    public static final int FULL_SURROUND_MASK = 15;

    public AutotileCell tileForMask(int mask) {
        if (mask < 0 || mask >= maskTiles.length) {
            throw new IllegalArgumentException("Invalid autotile mask " + mask + " for family " + id);
        }
        return maskTiles[mask];
    }

    public AutotileCell tileForFullSurround(int cellX, int cellY, int seed) {
        if (centerTiles.length == 0) {
            return tileForMask(FULL_SURROUND_MASK);
        }
        int index = AutotileResolver.positiveHash(cellX, cellY, seed) % centerTiles.length;
        return centerTiles[index];
    }
}
