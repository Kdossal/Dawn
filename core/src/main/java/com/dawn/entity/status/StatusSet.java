package com.dawn.entity.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Active derived statuses on an entity (recomputed each frame). */
public final class StatusSet {
    private final Set<StatusId> active = EnumSet.noneOf(StatusId.class);

    public void clear() {
        active.clear();
    }

    public void add(StatusId id) {
        if (id != null) {
            active.add(id);
        }
    }

    public boolean has(StatusId id) {
        return id != null && active.contains(id);
    }

    public boolean isEmpty() {
        return active.isEmpty();
    }

    public List<StatusId> ordered() {
        List<StatusId> ids = new ArrayList<>(active);
        Collections.sort(ids);
        return ids;
    }

    public String formatDisplayNames() {
        if (active.isEmpty()) {
            return "—";
        }
        List<StatusId> ids = ordered();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(StatusRegistry.displayName(ids.get(i)));
        }
        return sb.toString();
    }
}
