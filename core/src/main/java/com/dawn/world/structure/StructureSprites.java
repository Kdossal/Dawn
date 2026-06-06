package com.dawn.world.structure;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.visual.BlockVisualDef;
import com.dawn.world.block.visual.BlockVisualLayout;
import com.dawn.world.block.visual.BlockVisualRegistry;
import java.util.ArrayList;
import java.util.List;

/** Collects placed structure part sprites for rendering and highlights. */
public final class StructureSprites {
    public record Sprite(TextureRegion region, float px, float py, float w, float h) {}

    private StructureSprites() {}

    public static List<Sprite> collectPlaced(
            World world, DawnAssets assets, StructureKind kind, int anchorX, int anchorY) {
        ArrayList<Sprite> sprites = new ArrayList<>();
        for (StructurePartDef part : kind.parts()) {
            int cellX = anchorX + part.dx();
            int cellY = anchorY + part.dy();
            if (!world.inBounds(cellX, cellY) || world.getObject(cellX, cellY) != part.blockId()) {
                continue;
            }
            BlockVisualDef visual = BlockVisualRegistry.get(part.blockId());
            if (visual == null) {
                continue;
            }
            TextureRegion region = assets.tile(visual.texture());
            if (region == null) {
                continue;
            }
            float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
            sprites.add(new Sprite(region, rect[0], rect[1], rect[2], rect[3]));
        }
        return sprites;
    }

    /** Blueprint sprites for placement preview (ignores current block ids). */
    public static List<Sprite> collectBlueprint(DawnAssets assets, StructureKind kind, int anchorX, int anchorY) {
        ArrayList<Sprite> sprites = new ArrayList<>();
        for (StructurePartDef part : kind.parts()) {
            BlockVisualDef visual = BlockVisualRegistry.get(part.blockId());
            if (visual == null) {
                continue;
            }
            TextureRegion region = assets.tile(visual.texture());
            if (region == null) {
                continue;
            }
            int cellX = anchorX + part.dx();
            int cellY = anchorY + part.dy();
            float[] rect = BlockVisualLayout.rectPx(visual, cellX, cellY);
            sprites.add(new Sprite(region, rect[0], rect[1], rect[2], rect[3]));
        }
        return sprites;
    }
}
