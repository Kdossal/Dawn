package com.dawn.world.render.highlight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dawn.assets.DawnAssets;
import com.dawn.gameplay.placement.PlacementPreview;
import com.dawn.render.RenderColors;
import com.dawn.world.block.visual.BlockSpriteDraw;
import com.dawn.world.block.visual.BlockVisualDef;
import com.dawn.world.block.visual.BlockVisualRegistry;
import com.dawn.world.structure.StructureSprites;
import java.util.List;

/** Tinted placement preview (blue valid, red invalid). */
public final class PlacementGhostRenderer {
    private final StructureMaskHighlightRenderer structureMasks;

    public PlacementGhostRenderer(StructureMaskHighlightRenderer structureMasks) {
        this.structureMasks = structureMasks;
    }

    public void render(
            SpriteBatch batch,
            DawnAssets assets,
            List<PlacementPreview> previews,
            float alignOffsetX,
            float alignOffsetY) {
        for (PlacementPreview preview : previews) {
            if (preview instanceof PlacementPreview.FloorCell floor) {
                Color tint = floor.valid() ? RenderColors.PLACEMENT_VALID : RenderColors.PLACEMENT_INVALID;
                BlockSpriteDraw.drawTintedCell(
                        batch, assets, floor.cellX(), floor.cellY(), tint, alignOffsetX, alignOffsetY);
            } else if (preview instanceof PlacementPreview.BlockSprite block) {
                Color tint = block.valid() ? RenderColors.PLACEMENT_VALID : RenderColors.PLACEMENT_INVALID;
                BlockVisualDef visual = BlockVisualRegistry.get(block.blockId());
                if (visual != null) {
                    BlockSpriteDraw.drawTintedBlock(
                            batch, assets, visual, block.cellX(), block.cellY(), tint, alignOffsetX, alignOffsetY);
                }
            } else if (preview instanceof PlacementPreview.StructureMask mask) {
                Color tint = mask.valid() ? RenderColors.PLACEMENT_VALID : RenderColors.PLACEMENT_INVALID;
                List<StructureSprites.Sprite> sprites =
                        StructureSprites.collectBlueprint(assets, mask.kind(), mask.anchorX(), mask.anchorY());
                structureMasks.renderSprites(batch, sprites, tint, alignOffsetX, alignOffsetY);
            }
        }
    }
}
