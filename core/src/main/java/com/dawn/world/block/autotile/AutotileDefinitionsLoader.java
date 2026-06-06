package com.dawn.world.block.autotile;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.dawn.assets.BlockTextureId;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/** Loads {@link AutotileFamily} definitions from classpath {@code /autotiles.json}. */
public final class AutotileDefinitionsLoader {
    private static final String RESOURCE = "/autotiles.json";

    private AutotileDefinitionsLoader() {}

    public static EnumMap<BlockId, AutotileFamily> load() {
        InputStream stream = AutotileDefinitionsLoader.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException(
                    "Missing classpath resource "
                            + RESOURCE
                            + " — add core/src/main/resources/autotiles.json");
        }
        return parseJson(stream);
    }

    static EnumMap<BlockId, AutotileFamily> parseJson(InputStream stream) {
        String raw;
        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8)) {
            raw = scanner.useDelimiter("\\A").next();
        }
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(raw);
        JsonValue families = root.get("families");
        if (families == null || !families.isArray()) {
            throw new IllegalArgumentException("autotiles.json: missing \"families\" array");
        }
        EnumMap<BlockId, AutotileFamily> map = new EnumMap<>(BlockId.class);
        Set<String> seenIds = new HashSet<>();
        for (JsonValue element : families) {
            AutotileFamily family = parseFamily(element);
            if (!seenIds.add(family.id())) {
                throw new IllegalArgumentException("autotiles.json: duplicate family id \"" + family.id() + "\"");
            }
            if (map.containsKey(family.blockId())) {
                throw new IllegalArgumentException(
                        "autotiles.json: duplicate blockId \"" + family.blockId() + "\"");
            }
            map.put(family.blockId(), family);
        }
        return map;
    }

    private static AutotileFamily parseFamily(JsonValue element) {
        String id = requireString(element, "id");
        BlockId blockId = BlockId.valueOf(requireString(element, "blockId"));
        BlockTextureId texture = BlockTextureId.valueOf(requireString(element, "textureId"));
        int cols = requireInt(element, "cols");
        int rows = requireInt(element, "rows");
        int tileSizePx = requireInt(element, "tileSizePx");
        Layer neighborLayer = Layer.valueOf(requireString(element, "neighborLayer"));
        BlockId neighborBlockId = BlockId.valueOf(requireString(element, "neighborBlockId"));

        AutotileCell[] maskTiles = new AutotileCell[AutotileFamily.FULL_SURROUND_MASK];
        JsonValue tiles = element.get("tiles");
        if (tiles == null || !tiles.isArray()) {
            throw new IllegalArgumentException("autotiles.json: family \"" + id + "\" missing \"tiles\" array");
        }
        Set<Integer> seenMasks = new HashSet<>();
        for (JsonValue tile : tiles) {
            int mask = requireInt(tile, "mask");
            if (mask < 0 || mask >= AutotileFamily.FULL_SURROUND_MASK) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" mask out of range: " + mask);
            }
            if (!seenMasks.add(mask)) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" duplicate mask " + mask);
            }
            maskTiles[mask] = new AutotileCell(requireInt(tile, "col"), requireInt(tile, "row"));
        }
        for (int mask = 0; mask < AutotileFamily.FULL_SURROUND_MASK; mask++) {
            if (maskTiles[mask] == null) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" missing mask " + mask);
            }
        }

        AutotileCell[] centerTiles = parseCenterTiles(element, id, cols, rows);
        validateCellBounds(id, cols, rows, maskTiles, centerTiles);

        return new AutotileFamily(
                id,
                blockId,
                texture,
                cols,
                rows,
                tileSizePx,
                neighborLayer,
                neighborBlockId,
                maskTiles,
                centerTiles);
    }

    private static AutotileCell[] parseCenterTiles(JsonValue element, String id, int cols, int rows) {
        JsonValue centers = element.get("centerTiles");
        if (centers == null || !centers.isArray() || centers.size == 0) {
            throw new IllegalArgumentException("autotiles.json: family \"" + id + "\" missing \"centerTiles\"");
        }
        List<AutotileCell> cells = new ArrayList<>();
        for (JsonValue tile : centers) {
            cells.add(new AutotileCell(requireInt(tile, "col"), requireInt(tile, "row")));
        }
        return cells.toArray(AutotileCell[]::new);
    }

    private static void validateCellBounds(
            String id, int cols, int rows, AutotileCell[] maskTiles, AutotileCell[] centerTiles) {
        for (AutotileCell cell : maskTiles) {
            validateCell(id, cols, rows, cell);
        }
        for (AutotileCell cell : centerTiles) {
            validateCell(id, cols, rows, cell);
        }
    }

    private static void validateCell(String id, int cols, int rows, AutotileCell cell) {
        if (cell.col() < 0 || cell.col() >= cols || cell.row() < 0 || cell.row() >= rows) {
            throw new IllegalArgumentException(
                    "autotiles.json: family \""
                            + id
                            + "\" cell out of bounds ("
                            + cell.col()
                            + ","
                            + cell.row()
                            + ") for "
                            + cols
                            + "x"
                            + rows);
        }
    }

    private static String requireString(JsonValue object, String key) {
        JsonValue v = object.get(key);
        if (v == null || v.isNull()) {
            throw new IllegalArgumentException("autotiles.json: missing \"" + key + "\"");
        }
        return v.asString();
    }

    private static int requireInt(JsonValue object, String key) {
        JsonValue v = object.get(key);
        if (v == null || v.isNull()) {
            throw new IllegalArgumentException("autotiles.json: missing \"" + key + "\"");
        }
        return v.asInt();
    }
}
