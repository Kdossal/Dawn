package com.dawn.gameplay;

import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;

/** Resolved mining/dig target cell and max health. */
public record BreakTarget(int x, int y, Layer layer, BlockId blockId, float health) {}
