package com.dawn.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.gameplay.ClickHints;
import com.dawn.gameplay.ClickVerb;
import com.dawn.render.BatchDraw;
import com.dawn.render.RenderColors;
import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.DawnTypography.TextTier;
import com.dawn.ui.inventory.InventoryUiStyle;

/** Bottom-right HUD rows: [RMB Place] [LMB Mine] (LMB outermost). */
public final class ClickHintRenderer {
    private static final float MARGIN_RIGHT = 12f;
    private static final float MARGIN_BOTTOM = 22f;
    private static final float ICON_TEXT_GAP = 4f;
    private static final float BETWEEN_HINTS_GAP = 12f;
    private static final float ROW_PAD = 4f;
    private static final TextTier TEXT_TIER = TextTier.SM;
    private static final Color TEXT_COLOR = InventoryUiStyle.LABEL_COLOR;

    private ClickHintRenderer() {}

    public static void render(HudAssets hud, DawnAssets assets, ClickHints hints) {
        if (hints == null) {
            return;
        }

        TextureRegion lmbIcon = assets.uiCommon.leftClick;
        TextureRegion rmbIcon = assets.uiCommon.rightClick;
        float iconH = lmbIcon.getRegionHeight();
        float iconW = lmbIcon.getRegionWidth();

        float lmbClusterW = clusterWidth(hud, hints.left().label(), iconW);
        float rowW = lmbClusterW;
        float rowH = Math.max(iconH, lineHeight(hud));

        if (hints.rightOrNull() != null) {
            float rmbClusterW = clusterWidth(hud, hints.rightOrNull().label(), rmbIcon.getRegionWidth());
            rowW += BETWEEN_HINTS_GAP + rmbClusterW;
        }

        float rowX = Constants.HUD_WIDTH_PX - MARGIN_RIGHT - rowW;
        float rowY = MARGIN_BOTTOM;

        SpriteBatch batch = hud.batch;
        batch.begin();
        BatchDraw.tintedRect(
                batch,
                assets.whitePixel,
                rowX - ROW_PAD,
                rowY - ROW_PAD,
                rowW + ROW_PAD * 2f,
                rowH + ROW_PAD * 2f,
                RenderColors.DROP_LABEL_BG);

        float x = rowX + rowW - lmbClusterW;
        drawCluster(batch, hud, lmbIcon, iconW, iconH, hints.left().label(), x, rowY, rowH);

        if (hints.rightOrNull() != null) {
            float rmbClusterW = clusterWidth(hud, hints.rightOrNull().label(), rmbIcon.getRegionWidth());
            x = rowX + rowW - lmbClusterW - BETWEEN_HINTS_GAP - rmbClusterW;
            drawCluster(batch, hud, rmbIcon, rmbIcon.getRegionWidth(), rmbIcon.getRegionHeight(), hints.rightOrNull().label(), x, rowY, rowH);
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private static void drawCluster(
            SpriteBatch batch,
            HudAssets hud,
            TextureRegion icon,
            float iconW,
            float iconH,
            String label,
            float x,
            float rowY,
            float rowH) {
        float iconY = rowY + (rowH - iconH) / 2f;
        batch.draw(icon, x, iconY, iconW, iconH);
        DawnTypography.draw(
                hud.batch,
                hud.font,
                hud.layout,
                label,
                TEXT_TIER,
                TextContext.HUD,
                x + iconW + ICON_TEXT_GAP,
                rowY + (rowH + lineHeight(hud)) / 2f - 2f,
                TEXT_COLOR);
    }

    private static float clusterWidth(HudAssets hud, String label, float iconW) {
        DawnTypography.layout(hud.layout, hud.font, label, TEXT_TIER, TextContext.HUD);
        return iconW + ICON_TEXT_GAP + hud.layout.width;
    }

    private static float lineHeight(HudAssets hud) {
        DawnTypography.layout(hud.layout, hud.font, "Ay", TEXT_TIER, TextContext.HUD);
        return hud.layout.height;
    }
}
