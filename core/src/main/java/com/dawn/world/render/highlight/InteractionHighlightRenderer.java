package com.dawn.world.render.highlight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dawn.assets.DawnAssets;
import com.dawn.gameplay.InteractionHighlight;
import com.dawn.render.RenderColors;
import com.dawn.world.World;
import com.dawn.world.block.Layer;
import com.dawn.world.block.visual.BlockSpriteDraw;
import com.dawn.world.block.visual.BlockVisualDef;
import com.dawn.world.block.visual.BlockVisualRegistry;
import java.util.List;

/** Renders tool-filtered interaction highlights (masked structures + textured tiles). */
public final class InteractionHighlightRenderer {
    private final StructureMaskHighlightRenderer structureMasks;

    public InteractionHighlightRenderer(StructureMaskHighlightRenderer structureMasks) {
        this.structureMasks = structureMasks;
    }

    public void render(SpriteBatch batch, DawnAssets assets, World world, List<InteractionHighlight.Highlight> highlights) {
        Color tint = RenderColors.TARGET_HIGHLIGHT;
        for (InteractionHighlight.Highlight highlight : highlights) {
            if (highlight instanceof InteractionHighlight.Highlight.StructureMask mask) {
                structureMasks.render(batch, assets, world, mask.anchorX(), mask.anchorY(), mask.kind(), tint);
            } else if (highlight instanceof InteractionHighlight.Highlight.BlockSprite sprite) {
                renderBlockSprite(batch, assets, sprite, tint);
            }
        }
    }

    private static void renderBlockSprite(
            SpriteBatch batch, DawnAssets assets, InteractionHighlight.Highlight.BlockSprite sprite, Color tint) {
        if (sprite.layer() == Layer.FLOOR) {
            BlockSpriteDraw.drawTintedCell(batch, assets, sprite.cellX(), sprite.cellY(), tint);
            return;
        }
        BlockVisualDef visual = BlockVisualRegistry.get(sprite.blockId());
        if (visual == null) {
            return;
        }
        BlockSpriteDraw.drawTintedBlock(batch, assets, visual, sprite.cellX(), sprite.cellY(), tint);
    }
}
