package com.dawn.item;

import com.dawn.world.block.BlockId;
import com.dawn.world.structure.StructureKind;

/** What an inventory item places into the world. */
public sealed interface Placeable {
    record Ground(BlockId blockId) implements Placeable {}

    record Block(BlockId blockId) implements Placeable {}

    record Structure(StructureKind kind) implements Placeable {}
}
