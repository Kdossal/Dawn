package com.dawn.world.block.autotile;

/** One autotile cell selected when all {@code requiredMask} bits are present. */
public record NeighborRule(int requiredMask, int col, int row) {
    public AutotileCell cell() {
        return new AutotileCell(col, row);
    }
}
