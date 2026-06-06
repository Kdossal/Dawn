package com.dawn.gameplay.drops;

import com.badlogic.gdx.graphics.Color;
import com.dawn.ui.DawnTypography;
import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.DawnTypography.TextTier;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.render.BatchDraw;
import com.dawn.render.RenderColors;
import com.dawn.ui.HudAssets;
import java.util.List;

public final class DropRenderer {
    private static final float HOVER_RADIUS_CELLS = 1.25f;
    private static final float ICON_SIZE_PX = Constants.CELL_SIZE_PX * 0.75f;
    private static final float ICON_HALF_SIZE_CELLS = ICON_SIZE_PX / Constants.CELL_SIZE_PX / 2f;
    private static final float LABEL_PAD = 5f;

    private final GlyphLayout layout = new GlyphLayout();

    public WorldDrop findHovered(List<WorldDrop> drops, float worldX, float worldY) {
        WorldDrop best = null;
        float bestDist = Float.MAX_VALUE;
        for (WorldDrop drop : drops) {
            float dx = drop.x - worldX;
            float dy = drop.y - worldY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist <= HOVER_RADIUS_CELLS && dist < bestDist) {
                bestDist = dist;
                best = drop;
            }
        }
        return best;
    }

    public static float iconHalfSizeCells() {
        return ICON_HALF_SIZE_CELLS;
    }

    public void renderSprites(SpriteBatch batch, DawnAssets assets, List<WorldDrop> drops) {
        batch.setColor(Color.WHITE);
        for (WorldDrop drop : drops) {
            drawOne(batch, assets, drop);
        }
        batch.setColor(Color.WHITE);
    }

    public static void drawOne(SpriteBatch batch, DawnAssets assets, WorldDrop drop) {
        if (drop.stack.isEmpty()) {
            return;
        }
        ItemDef def = ItemRegistry.get(drop.stack);
        if (def == null) {
            return;
        }
        TextureRegion icon = assets.item(def.iconId());
        if (icon == null) {
            return;
        }
        float cellSize = Constants.CELL_SIZE_PX;
        float px = drop.x * cellSize - ICON_SIZE_PX / 2f;
        float py = drop.y * cellSize - ICON_SIZE_PX / 2f;
        batch.draw(icon, px, py, ICON_SIZE_PX, ICON_SIZE_PX);
    }

    public void renderHoverLabel(HudAssets hud, DawnAssets assets, WorldDrop hovered, float screenX, float screenY) {
        if (hovered == null || hovered.stack.isEmpty()) {
            return;
        }
        ItemDef def = ItemRegistry.get(hovered.stack);
        if (def == null) {
            return;
        }
        String label = def.displayName();
        if (hovered.stack.count > 1) {
            label += " x" + hovered.stack.count;
        }
        DawnTypography.layout(layout, hud.font, label, TextTier.SM, TextContext.HUD);
        float boxW = layout.width + LABEL_PAD * 2f;
        float boxH = layout.height + LABEL_PAD * 2f;
        float boxX = screenX - boxW / 2f;
        float boxY = screenY + 14f;

        SpriteBatch batch = hud.batch;
        batch.begin();
        BatchDraw.tintedRect(batch, assets.whitePixel, boxX, boxY, boxW, boxH, RenderColors.DROP_LABEL_BG);
        DawnTypography.draw(
                hud.font,
                batch,
                layout,
                boxX + LABEL_PAD,
                boxY + LABEL_PAD + layout.height,
                TextTier.SM,
                TextContext.HUD,
                Color.WHITE);
        batch.end();
    }
}
