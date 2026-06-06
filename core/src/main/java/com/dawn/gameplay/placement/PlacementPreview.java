package com.dawn.gameplay.placement;

import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import com.dawn.world.structure.StructureKind;

/** Ghost geometry for a placeable preview. */
public sealed interface PlacementPreview {
    record FloorCell(int cellX, int cellY, boolean valid) implements PlacementPreview {}

    record BlockSprite(int cellX, int cellY, BlockId blockId, boolean valid) implements PlacementPreview {}

    record StructureMask(int anchorX, int anchorY, StructureKind kind, boolean valid) implements PlacementPreview {}
}
