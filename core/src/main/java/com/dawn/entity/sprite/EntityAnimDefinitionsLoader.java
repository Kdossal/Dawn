package com.dawn.entity.sprite;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.dawn.entity.EntityId;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/** Loads {@link EntityAnimDef} from classpath {@code /entity_animations.json}. */
public final class EntityAnimDefinitionsLoader {
    private static final String RESOURCE = "/entity_animations.json";

    private EntityAnimDefinitionsLoader() {}

    public static EnumMap<EntityId, EntityAnimDef> load() {
        InputStream stream = EntityAnimDefinitionsLoader.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException(
                    "Missing classpath resource "
                            + RESOURCE
                            + " — add core/src/main/resources/entity_animations.json");
        }
        return parseJson(stream);
    }

    static EnumMap<EntityId, EntityAnimDef> parseJson(InputStream stream) {
        String raw;
        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8)) {
            raw = scanner.useDelimiter("\\A").next();
        }
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(raw);
        JsonValue entities = root.get("entities");
        if (entities == null || !entities.isArray()) {
            throw new IllegalArgumentException("entity_animations.json: missing \"entities\" array");
        }
        EnumMap<EntityId, EntityAnimDef> map = new EnumMap<>(EntityId.class);
        for (JsonValue element : entities) {
            EntityAnimDef def = parseEntity(element);
            if (map.containsKey(def.entityId())) {
                throw new IllegalArgumentException(
                        "entity_animations.json: duplicate entityId \"" + def.entityId() + "\"");
            }
            map.put(def.entityId(), def);
        }
        return map;
    }

    private static EntityAnimDef parseEntity(JsonValue element) {
        EntityId entityId = EntityId.valueOf(requireString(element, "entityId"));
        String spriteId = requireString(element, "spriteId");
        int frameWidth = requireInt(element, "frameWidth");
        int frameHeight = requireInt(element, "frameHeight");
        int cols = requireInt(element, "cols");
        int rows = requireInt(element, "rows");
        if (frameWidth <= 0 || frameHeight <= 0 || cols <= 0 || rows <= 0) {
            throw new IllegalArgumentException("entity_animations.json: invalid grid for " + entityId);
        }

        JsonValue clipsNode = element.get("clips");
        if (clipsNode == null || !clipsNode.isObject()) {
            throw new IllegalArgumentException("entity_animations.json: missing \"clips\" for " + entityId);
        }
        Map<String, EntityAnimClip> clips = new HashMap<>();
        Set<Integer> usedRows = new HashSet<>();
        for (JsonValue clipNode : clipsNode) {
            String clipId = clipNode.name;
            int row = requireInt(clipNode, "row");
            int frameCount = requireInt(clipNode, "frameCount");
            float fps = requireFloat(clipNode, "fps");
            if (row < 0 || row >= rows) {
                throw new IllegalArgumentException(
                        "entity_animations.json: clip \"" + clipId + "\" row out of bounds for " + entityId);
            }
            if (frameCount <= 0 || frameCount > cols) {
                throw new IllegalArgumentException(
                        "entity_animations.json: clip \"" + clipId + "\" invalid frameCount for " + entityId);
            }
            if (fps <= 0f) {
                throw new IllegalArgumentException(
                        "entity_animations.json: clip \"" + clipId + "\" invalid fps for " + entityId);
            }
            if (!usedRows.add(row)) {
                throw new IllegalArgumentException(
                        "entity_animations.json: duplicate row " + row + " for " + entityId);
            }
            clips.put(clipId, new EntityAnimClip(row, frameCount, fps));
        }
        return new EntityAnimDef(entityId, spriteId, frameWidth, frameHeight, cols, rows, Map.copyOf(clips));
    }

    private static String requireString(JsonValue object, String key) {
        JsonValue v = object.get(key);
        if (v == null || v.isNull()) {
            throw new IllegalArgumentException("entity_animations.json: missing \"" + key + "\"");
        }
        return v.asString();
    }

    private static int requireInt(JsonValue object, String key) {
        JsonValue v = object.get(key);
        if (v == null || v.isNull()) {
            throw new IllegalArgumentException("entity_animations.json: missing \"" + key + "\"");
        }
        return v.asInt();
    }

    private static float requireFloat(JsonValue object, String key) {
        JsonValue v = object.get(key);
        if (v == null || v.isNull()) {
            throw new IllegalArgumentException("entity_animations.json: missing \"" + key + "\"");
        }
        return v.asFloat();
    }
}
