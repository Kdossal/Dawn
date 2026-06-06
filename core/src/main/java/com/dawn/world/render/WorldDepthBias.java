package com.dawn.world.render;

import com.dawn.world.block.BlockId;

/** Draw order within the same sort cell. */
public final class WorldDepthBias {
    public static final int ENTITY = 100;
    public static final int DROP = 200;

    private WorldDepthBias() {}

    public static int block(BlockId id) {
        return id.ordinal();
    }
}
