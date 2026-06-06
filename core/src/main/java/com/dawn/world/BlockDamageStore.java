package com.dawn.world;

import com.dawn.world.block.Layer;
import java.util.HashMap;
import java.util.Map;

/** Remaining break health per world cell (persists when mining stops). */
public final class BlockDamageStore {
    private final Map<Long, Float> remaining = new HashMap<>();

    public float getRemaining(Layer layer, int x, int y, float defaultMax) {
        Float v = remaining.get(CellPos.pack(layer, x, y));
        return v == null ? defaultMax : v;
    }

    public void setRemaining(Layer layer, int x, int y, float value, float defaultMax) {
        long key = CellPos.pack(layer, x, y);
        if (value >= defaultMax - 1e-4f) {
            remaining.remove(key);
        } else {
            remaining.put(key, Math.max(0f, value));
        }
    }

    public void clear(Layer layer, int x, int y) {
        remaining.remove(CellPos.pack(layer, x, y));
    }
}
