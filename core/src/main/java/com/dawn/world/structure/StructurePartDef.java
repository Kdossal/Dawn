package com.dawn.world.structure;

import com.dawn.world.block.BlockId;

/** One tile of a structure relative to its anchor (bottom-left / root cell). */
public record StructurePartDef(int dx, int dy, BlockId blockId, int breakOrder) {}
