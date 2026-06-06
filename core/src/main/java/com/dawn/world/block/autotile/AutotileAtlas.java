package com.dawn.world.block.autotile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.BlockTextureId;
import java.util.EnumMap;
import java.util.Map;

/** Splits autotile sheet textures into per-cell regions. */
public final class AutotileAtlas {
    private final Map<BlockTextureId, TextureRegion[][]> sheets = new EnumMap<>(BlockTextureId.class);

    public static AutotileAtlas build(Map<String, TextureRegion> tilesByFileName) {
        AutotileAtlas atlas = new AutotileAtlas();
        for (AutotileFamily family : AutotileRegistry.allFamilies()) {
            if (atlas.sheets.containsKey(family.texture())) {
                continue;
            }
            TextureRegion sheet = tilesByFileName.get(family.texture().fileName);
            if (sheet == null) {
                throw new IllegalStateException(
                        "Missing autotile sheet texture: tiles/" + family.texture().fileName + ".png");
            }
            atlas.registerSheet(family, sheet);
        }
        return atlas;
    }

    private void registerSheet(AutotileFamily family, TextureRegion sheet) {
        int expectedW = family.cols() * family.tileSizePx();
        int expectedH = family.rows() * family.tileSizePx();
        if (sheet.getRegionWidth() != expectedW || sheet.getRegionHeight() != expectedH) {
            throw new IllegalStateException(
                    "Autotile sheet tiles/"
                            + family.texture().fileName
                            + ".png expected "
                            + expectedW
                            + "x"
                            + expectedH
                            + " but was "
                            + sheet.getRegionWidth()
                            + "x"
                            + sheet.getRegionHeight());
        }
        TextureRegion[][] split = sheet.split(family.tileSizePx(), family.tileSizePx());
        if (split == null || split.length != family.rows() || split[0].length != family.cols()) {
            throw new IllegalStateException(
                    "Failed to split autotile sheet tiles/" + family.texture().fileName + ".png");
        }
        sheets.put(family.texture(), transposeRowMajorToColMajor(split, family.cols(), family.rows()));
    }

    /**
     * {@link TextureRegion#split} returns row-major with the first index as row (Y). We store
     * {@code cells[col][row]} for direct lookup.
     */
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

    public TextureRegion region(BlockTextureId texture, int col, int row) {
        TextureRegion[][] cells = sheets.get(texture);
        if (cells == null) {
            return null;
        }
        if (col < 0 || col >= cells.length || row < 0 || row >= cells[0].length) {
            return null;
        }
        return cells[col][row];
    }
}
