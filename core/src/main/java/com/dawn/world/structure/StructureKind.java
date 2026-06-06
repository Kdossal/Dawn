package com.dawn.world.structure;

import com.dawn.world.block.BlockId;

/** Blueprint for a placed multi-cell object. */
public enum StructureKind {
    BED(
            StructureBreakPolicy.BREAK_ENTIRE_STRUCTURE,
            new StructurePartDef(0, 0, BlockId.BED_FOOT, 0),
            new StructurePartDef(0, 1, BlockId.BED_HEAD, 0));

    private final StructureBreakPolicy breakPolicy;
    private final StructurePartDef[] parts;

    StructureKind(StructureBreakPolicy breakPolicy, StructurePartDef... parts) {
        this.breakPolicy = breakPolicy;
        this.parts = parts;
    }

    public StructureBreakPolicy breakPolicy() {
        return breakPolicy;
    }

    public StructurePartDef[] parts() {
        return parts;
    }

    public String breakMessageFor(BlockId brokenPart) {
        return switch (this) {
            case BED -> "Broke bed";
        };
    }
}
