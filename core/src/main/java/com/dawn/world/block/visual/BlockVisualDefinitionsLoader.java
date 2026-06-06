package com.dawn.world.block.visual;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.dawn.assets.BlockTextureId;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Loads {@link BlockVisualDef} from classpath {@code /block_visuals.json}. Validates coverage for every
 * non-AIR block on GROUND, FLOOR, or OBJECT layers.
 */
public final class BlockVisualDefinitionsLoader {
    private static final String RESOURCE = "/block_visuals.json";

    private BlockVisualDefinitionsLoader() {}

    public static EnumMap<BlockId, BlockVisualDef> load() {
        InputStream stream = BlockVisualDefinitionsLoader.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException(
                    "Missing classpath resource "
                            + RESOURCE
                            + " — add core/src/main/resources/block_visuals.json");
        }
        EnumMap<BlockId, BlockVisualDef> map = parseJson(stream);
        validateRequiredBlockIdsPresent(map);
        return map;
    }

    static EnumMap<BlockId, BlockVisualDef> parseJson(InputStream stream) {
        String raw;
        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8)) {
            raw = scanner.useDelimiter("\\A").next();
        }
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(raw);
        JsonValue entries = root.get("entries");
        if (entries == null || !entries.isArray()) {
            throw new IllegalArgumentException("block_visuals.json: missing \"entries\" array");
        }
        EnumMap<BlockId, BlockVisualDef> map = new EnumMap<>(BlockId.class);
        Set<BlockId> seen = new HashSet<>();
        for (JsonValue element : entries) {
            BlockId id = BlockId.valueOf(requireString(element, "blockId"));
            if (!seen.add(id)) {
                throw new IllegalArgumentException("block_visuals.json: duplicate blockId \"" + id + "\"");
            }
            BlockTextureId texture = BlockTextureId.valueOf(requireString(element, "textureId"));
            int width = requireInt(element, "width");
            int height = requireInt(element, "height");
            VisualAnchor anchor = VisualAnchor.valueOf(requireString(element, "anchor"));
            int offsetX = element.has("offsetX") ? element.getInt("offsetX") : 0;
            int offsetY = element.has("offsetY") ? element.getInt("offsetY") : 0;
            float alpha = element.has("alpha") ? element.getFloat("alpha") : 1f;
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "block_visuals.json: invalid size for " + id + " (" + width + "x" + height + ")");
            }
            map.put(id, BlockVisualDef.of(texture, width, height, anchor, offsetX, offsetY, alpha));
        }
        return map;
    }

    private static void validateRequiredBlockIdsPresent(EnumMap<BlockId, BlockVisualDef> map) {
        for (BlockId id : BlockId.values()) {
            if (id == BlockId.AIR) {
                continue;
            }
            BlockDefinitions.BlockDef def = BlockDefinitions.get(id);
            if (def == null) {
                continue;
            }
            Layer layer = def.layer();
            if (layer != Layer.GROUND && layer != Layer.FLOOR && layer != Layer.OBJECT) {
                continue;
            }
            if (!map.containsKey(id)) {
                throw new IllegalStateException(
                        "block_visuals.json: entry required for layer "
                                + layer
                                + " block "
                                + id);
            }
        }
    }

    private static String requireString(JsonValue object, String key) {
        JsonValue v = object.get(key);
        if (v == null || v.isNull()) {
            throw new IllegalArgumentException("block_visuals.json: missing \"" + key + "\" on entry");
        }
        return v.asString();
    }

    private static int requireInt(JsonValue object, String key) {
        JsonValue v = object.get(key);
        if (v == null || v.isNull()) {
            throw new IllegalArgumentException("block_visuals.json: missing \"" + key + "\" on entry");
        }
        return v.asInt();
    }
}
