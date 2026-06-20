package com.dawn.world.render;

import com.dawn.world.block.BlockId;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.Layer;

/** Back-to-front ground draw order for layered terrain rendering. */
public enum GroundDrawPass {
    FLUID(BlockId.PIT, BlockId.WATER),
    STONE(BlockId.STONE_GROUND),
    SAND(BlockId.SAND_GROUND),
    DIRT(BlockId.DIRT_GROUND);

    private static final GroundDrawPass[] GROUND_PASSES = values();

    private final BlockId[] blockIds;

    GroundDrawPass(BlockId... blockIds) {
        this.blockIds = blockIds;
    }

    public static GroundDrawPass[] groundPasses() {
        return GROUND_PASSES;
    }

    public static GroundDrawPass forGround(BlockId id) {
        if (id == null || id == BlockId.AIR) {
            return null;
        }
        if (BlockDefinitions.get(id) == null || BlockDefinitions.get(id).layer() != Layer.GROUND) {
            return null;
        }
        for (GroundDrawPass pass : GROUND_PASSES) {
            if (pass.matches(id)) {
                return pass;
            }
        }
        return null;
    }

    public boolean matches(BlockId id) {
        if (id == null) {
            return false;
        }
        for (BlockId candidate : blockIds) {
            if (candidate == id) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFloorBlock(BlockId id) {
        if (id == null || id == BlockId.AIR) {
            return false;
        }
        var def = BlockDefinitions.get(id);
        return def != null && def.layer() == Layer.FLOOR;
    }
}
