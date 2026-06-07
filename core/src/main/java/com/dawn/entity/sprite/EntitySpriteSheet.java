package com.dawn.entity.sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.entity.EntityId;
import java.util.EnumMap;
import java.util.Map;

/** Splits animated entity sheets into per-frame regions. */
public final class EntitySpriteSheet {
    private final Map<EntityId, TextureRegion[][]> sheets = new EnumMap<>(EntityId.class);
    private final Map<EntityId, EntityAnimDef> defs = new EnumMap<>(EntityId.class);

    public static EntitySpriteSheet build(Map<String, TextureRegion> entitiesBySpriteId) {
        EntitySpriteSheet atlas = new EntitySpriteSheet();
        for (EntityId id : EntityId.values()) {
            EntityAnimDef def = EntityAnimRegistry.get(id);
            if (def != null) {
                atlas.register(def, entitiesBySpriteId);
            }
        }
        return atlas;
    }

    private void register(EntityAnimDef def, Map<String, TextureRegion> entitiesBySpriteId) {
        TextureRegion sheet = entitiesBySpriteId.get(def.spriteId());
        if (sheet == null) {
            throw new IllegalStateException(
                    "Missing animated entity sheet: entities/" + def.spriteId() + ".png");
        }
        int expectedW = def.cols() * def.frameWidth();
        int expectedH = def.rows() * def.frameHeight();
        if (sheet.getRegionWidth() != expectedW || sheet.getRegionHeight() != expectedH) {
            throw new IllegalStateException(
                    "Entity sheet entities/"
                            + def.spriteId()
                            + ".png expected "
                            + expectedW
                            + "x"
                            + expectedH
                            + " but was "
                            + sheet.getRegionWidth()
                            + "x"
                            + sheet.getRegionHeight());
        }
        TextureRegion[][] split = sheet.split(def.frameWidth(), def.frameHeight());
        if (split == null || split.length != def.rows() || split[0].length != def.cols()) {
            throw new IllegalStateException("Failed to split entity sheet entities/" + def.spriteId() + ".png");
        }
        sheets.put(def.entityId(), transposeRowMajorToColMajor(split, def.cols(), def.rows()));
        defs.put(def.entityId(), def);
    }

    private static TextureRegion[][] transposeRowMajorToColMajor(
            TextureRegion[][] split, int cols, int rows) {
        TextureRegion[][] cells = new TextureRegion[cols][rows];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[col][row] = split[row][col];
            }
        }
        return cells;
    }

    public EntityAnimDef def(EntityId entityId) {
        return defs.get(entityId);
    }

    public TextureRegion frame(EntityId entityId, int col, int row) {
        TextureRegion[][] cells = sheets.get(entityId);
        if (cells == null) {
            return null;
        }
        if (col < 0 || col >= cells.length || row < 0 || row >= cells[0].length) {
            return null;
        }
        return cells[col][row];
    }
}
