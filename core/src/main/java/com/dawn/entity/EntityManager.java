package com.dawn.entity;

import java.util.ArrayList;
import java.util.List;

public final class EntityManager {
    private final List<Entity> entities = new ArrayList<>();
    private Entity player;

    public Entity spawn(EntityId id, float x, float y) {
        Entity entity = new Entity(id, x, y);
        entities.add(entity);
        if (id == EntityId.PLAYER) {
            player = entity;
        }
        return entity;
    }

    public Entity getPlayer() {
        return player;
    }

}
