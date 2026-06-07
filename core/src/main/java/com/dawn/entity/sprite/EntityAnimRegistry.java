package com.dawn.entity.sprite;

import com.dawn.entity.EntityId;
import java.util.Map;

public final class EntityAnimRegistry {
    private static final Map<EntityId, EntityAnimDef> DEFS = EntityAnimDefinitionsLoader.load();

    private EntityAnimRegistry() {}

    public static EntityAnimDef get(EntityId entityId) {
        if (entityId == null) {
            return null;
        }
        return DEFS.get(entityId);
    }
}
