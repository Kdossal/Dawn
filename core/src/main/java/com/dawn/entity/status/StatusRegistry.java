package com.dawn.entity.status;

import java.util.EnumMap;
import java.util.Map;

public final class StatusRegistry {
    private static final Map<StatusId, StatusDef> DEFS = new EnumMap<>(StatusId.class);

    static {
        register(new StatusDef(StatusId.BURDENED, "Burdened"));
        register(new StatusDef(StatusId.IMMOBILE, "Immobile"));
        register(new StatusDef(StatusId.HUNGRY, "Hungry"));
        register(new StatusDef(StatusId.STARVING, "Starving"));
        register(new StatusDef(StatusId.POISONED, "Poisoned"));
    }

    private StatusRegistry() {}

    private static void register(StatusDef def) {
        DEFS.put(def.id(), def);
    }

    public static StatusDef get(StatusId id) {
        return id == null ? null : DEFS.get(id);
    }

    public static String displayName(StatusId id) {
        StatusDef def = get(id);
        return def == null ? id.name() : def.displayName();
    }
}
