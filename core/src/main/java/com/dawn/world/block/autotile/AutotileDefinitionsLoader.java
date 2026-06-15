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
import java.util.Locale;
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
        int tileWidthPx = element.has("tileWidthPx") ? element.getInt("tileWidthPx") : tileSizePx;
        int tileHeightPx = element.has("tileHeightPx") ? element.getInt("tileHeightPx") : tileSizePx;
        if (tileWidthPx <= 0 || tileHeightPx <= 0) {
            throw new IllegalArgumentException(
                    "autotiles.json: family \"" + id + "\" tileWidthPx and tileHeightPx must be > 0");
        }
        Layer neighborLayer = Layer.valueOf(requireString(element, "neighborLayer"));
        BlockId neighborBlockId = BlockId.valueOf(requireString(element, "neighborBlockId"));
        AutotileLayout layout = parseLayout(element);

        AutotileCell[] maskTiles;
        AutotileCell[] centerTiles;
        NeighborRule[] neighborRules;

        if (layout == AutotileLayout.STANDARD_BLOB) {
            if (element.has("tiles") || element.has("neighborRules") || element.has("neighborRulesFile")) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" STANDARD_BLOB must not define neighbor rules");
            }
            maskTiles = StandardBlobLayout.buildMaskTiles();
            centerTiles = resolveStandardBlobCenterTiles(element, id, cols);
            if (centerTiles.length == 0) {
                maskTiles[AutotileFamily.FULL_SURROUND_MASK] = StandardBlobLayout.fullSurroundCell();
            }
            neighborRules = new NeighborRule[0];
        } else if (layout == AutotileLayout.NEIGHBOR_RULES) {
            if (element.has("tiles")) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" NEIGHBOR_RULES must not define \"tiles\"");
            }
            maskTiles = new AutotileCell[AutotileFamily.MASK_COUNT];
            centerTiles = new AutotileCell[0];
            neighborRules = NeighborRuleMatcher.sortForMatch(parseNeighborRules(element, id));
        } else {
            maskTiles = parseCustomMaskTiles(element, id);
            centerTiles = parseCenterTiles(element);
            if (centerTiles.length > 0) {
                if (maskTiles[AutotileFamily.FULL_SURROUND_MASK] != null) {
                    throw new IllegalArgumentException(
                            "autotiles.json: family \""
                                    + id
                                    + "\" cannot define mask 15 in tiles when centerTiles is set");
                }
            } else if (maskTiles[AutotileFamily.FULL_SURROUND_MASK] == null) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" missing mask 15 or centerTiles");
            }
            neighborRules = new NeighborRule[0];
        }

        validateCellBounds(id, cols, rows, maskTiles, centerTiles, neighborRules);

        return new AutotileFamily(
                id,
                blockId,
                texture,
                cols,
                rows,
                tileWidthPx,
                tileHeightPx,
                neighborLayer,
                neighborBlockId,
                maskTiles,
                centerTiles,
                neighborRules);
    }

    private static NeighborRule[] parseNeighborRules(JsonValue element, String id) {
        JsonValue inline = element.get("neighborRules");
        if (inline != null && inline.isArray()) {
            return parseNeighborRulesArray(inline, id);
        }
        JsonValue fileRef = element.get("neighborRulesFile");
        if (fileRef != null && !fileRef.isNull()) {
            String path = fileRef.asString();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            InputStream stream = AutotileDefinitionsLoader.class.getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" missing neighbor rules resource " + path);
            }
            String raw;
            try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8)) {
                raw = scanner.useDelimiter("\\A").next();
            }
            JsonValue rulesRoot = new JsonReader().parse(raw);
            if (!rulesRoot.isArray()) {
                throw new IllegalArgumentException(
                        "autotiles.json: neighbor rules file for family \"" + id + "\" must be a JSON array");
            }
            return parseNeighborRulesArray(rulesRoot, id);
        }
        throw new IllegalArgumentException(
                "autotiles.json: family \"" + id + "\" NEIGHBOR_RULES requires neighborRules or neighborRulesFile");
    }

    private static NeighborRule[] parseNeighborRulesArray(JsonValue rules, String id) {
        List<NeighborRule> parsed = new ArrayList<>();
        for (JsonValue rule : rules) {
            int col = requireInt(rule, "col");
            int row = requireInt(rule, "row");
            JsonValue neighbors = rule.get("neighbors");
            if (neighbors == null || !neighbors.isArray()) {
                throw new IllegalArgumentException(
                        "autotiles.json: family \"" + id + "\" neighbor rule missing neighbors array");
            }
            String[] names = new String[neighbors.size];
            for (int i = 0; i < neighbors.size; i++) {
                names[i] = neighbors.get(i).asString();
            }
            int requiredMask = EightNeighborMask.maskFromNames(names);
            parsed.add(new NeighborRule(requiredMask, col, row));
        }
        if (parsed.isEmpty()) {
            throw new IllegalArgumentException(
                    "autotiles.json: family \"" + id + "\" neighborRules must not be empty");
        }
        return parsed.toArray(NeighborRule[]::new);
    }

    private static AutotileLayout parseLayout(JsonValue element) {
        JsonValue layoutValue = element.get("layout");
        if (layoutValue == null || layoutValue.isNull()) {
            return AutotileLayout.CUSTOM;
        }
        return AutotileLayout.valueOf(layoutValue.asString().toUpperCase(Locale.ROOT));
    }

    private static AutotileCell[] resolveStandardBlobCenterTiles(JsonValue element, String id, int cols) {
        AutotileCell[] explicit = parseCenterTiles(element);
        if (explicit.length > 0) {
            return explicit;
        }
        JsonValue centerRowValue = element.get("centerRow");
        if (centerRowValue == null || centerRowValue.isNull()) {
            return new AutotileCell[0];
        }
        int centerRow = centerRowValue.asInt();
        if (centerRow < 0) {
            throw new IllegalArgumentException(
                    "autotiles.json: family \"" + id + "\" centerRow must be >= 0");
        }
        return StandardBlobLayout.centerTilesFromRow(cols, centerRow);
    }

    private static AutotileCell[] parseCustomMaskTiles(JsonValue element, String id) {
        AutotileCell[] maskTiles = new AutotileCell[AutotileFamily.MASK_COUNT];
        JsonValue tiles = element.get("tiles");
        if (tiles == null || !tiles.isArray()) {
            throw new IllegalArgumentException("autotiles.json: family \"" + id + "\" missing \"tiles\" array");
        }
        Set<Integer> seenMasks = new HashSet<>();
        for (JsonValue tile : tiles) {
            int mask = requireInt(tile, "mask");
            if (mask < 0 || mask >= AutotileFamily.MASK_COUNT) {
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
        return maskTiles;
    }

    private static AutotileCell[] parseCenterTiles(JsonValue element) {
        JsonValue centers = element.get("centerTiles");
        if (centers == null || !centers.isArray() || centers.size == 0) {
            return new AutotileCell[0];
        }
        List<AutotileCell> cells = new ArrayList<>();
        for (JsonValue tile : centers) {
            cells.add(new AutotileCell(requireInt(tile, "col"), requireInt(tile, "row")));
        }
        return cells.toArray(AutotileCell[]::new);
    }

    private static void validateCellBounds(
            String id,
            int cols,
            int rows,
            AutotileCell[] maskTiles,
            AutotileCell[] centerTiles,
            NeighborRule[] neighborRules) {
        for (AutotileCell cell : maskTiles) {
            if (cell != null) {
                validateCell(id, cols, rows, cell);
            }
        }
        for (AutotileCell cell : centerTiles) {
            validateCell(id, cols, rows, cell);
        }
        for (NeighborRule rule : neighborRules) {
            validateCell(id, cols, rows, rule.cell());
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
