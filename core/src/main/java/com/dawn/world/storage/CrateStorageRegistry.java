package com.dawn.world.storage;

import com.dawn.world.CellPos;
import java.util.HashMap;
import java.util.Map;

/** Cell-keyed crate storage instances (one per placed crate). */
public final class CrateStorageRegistry {
    private final Map<Long, CrateStorage> byCell = new HashMap<>();

    public CrateStorage createAt(int x, int y) {
        long key = CellPos.pack(x, y);
        CrateStorage storage = new CrateStorage();
        byCell.put(key, storage);
        return storage;
    }

    public CrateStorage getAt(int x, int y) {
        return byCell.get(CellPos.pack(x, y));
    }

    public boolean hasAt(int x, int y) {
        return byCell.containsKey(CellPos.pack(x, y));
    }

    public CrateStorage removeAt(int x, int y) {
        return byCell.remove(CellPos.pack(x, y));
    }
}
