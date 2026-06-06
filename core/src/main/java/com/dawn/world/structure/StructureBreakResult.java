package com.dawn.world.structure;

import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import java.util.List;

/** Blocks cleared by a structure break and where to spawn loot. */
public record StructureBreakResult(List<PartBreak> partBreaks, String message) {
    public record PartBreak(int x, int y, Layer layer, BlockId blockId) {}
}
