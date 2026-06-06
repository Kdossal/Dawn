package com.dawn.world;

import com.dawn.world.block.Layer;

/** Packed cell coordinates for map keys. */
public final class CellPos {
    private CellPos() {}

    public static long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    public static long pack(Layer layer, int x, int y) {
        return ((long) layer.ordinal() << 48) | ((long) x << 16) | (y & 0xffffL);
    }

    public static int unpackX(long packed) {
        return (int) (packed >> 32);
    }

    public static int unpackY(long packed) {
        return (int) packed;
    }
}
