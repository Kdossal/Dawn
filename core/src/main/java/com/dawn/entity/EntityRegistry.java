package com.dawn.entity;

import com.dawn.config.Constants;
import java.util.EnumMap;
import java.util.Map;

public final class EntityRegistry {
    private static final Map<EntityId, EntityDef> DEFS = new EnumMap<>(EntityId.class);

    static {
        register(
                new EntityDef(
                        EntityId.PLAYER,
                        "Traveler",
                        "player",
                        Constants.DEFAULT_MOVE_WIDTH_CELLS,
                        Constants.DEFAULT_MOVE_HEIGHT_CELLS,
                        Stats.withUniformBase(10)));
    }

    private EntityRegistry() {}

    private static void register(EntityDef def) {
        DEFS.put(def.id(), def);
    }

    public static EntityDef get(EntityId id) {
        return id == null ? null : DEFS.get(id);
    }
}
