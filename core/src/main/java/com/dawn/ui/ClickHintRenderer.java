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

/** HUD hints: click actions bottom-right, key actions bottom-left. */
public final class ClickHintRenderer {
    private static final float MARGIN_RIGHT = 12f;
    private static final float MARGIN_LEFT = 12f;
    private static final float MARGIN_BOTTOM = 22f;
    private static final float ICON_TEXT_GAP = 4f;
    private static final float BETWEEN_HINTS_GAP = 12f;
    private static final float ROW_PAD = 4f;
    private static final TextTier TEXT_TIER = TextTier.SM;
    private static final Color TEXT_COLOR = InventoryUiStyle.LABEL_COLOR;

    private ClickHintRenderer() {}

    public static void render(
            HudAssets hud,
            DawnAssets assets,
            ClickHints hints,
            boolean showDropHint,
            boolean showInventoryHint) {
        SpriteBatch batch = hud.batch;
        batch.begin();
        renderClickHints(batch, hud, assets, hints);
        renderKeyHints(batch, hud, assets, showDropHint, showInventoryHint);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private static void renderClickHints(
            SpriteBatch batch, HudAssets hud, DawnAssets assets, ClickHints hints) {
        if (hints == null) {
            return;
        }
        TextureRegion lmbIcon = assets.uiCommon.leftClick;
        TextureRegion rmbIcon = assets.uiCommon.rightClick;
        ClickVerb left = hints.left();
        ClickVerb right = hints.rightOrNull();
        if (left == null && right == null) {
            return;
        }

        float rowH = Math.max(Math.max(lmbIcon.getRegionHeight(), rmbIcon.getRegionHeight()), lineHeight(hud));
        float rowW = 0f;
        if (left != null) {
            rowW += clusterWidth(hud, left.label(), lmbIcon.getRegionWidth());
        }
        if (right != null) {
            if (rowW > 0f) {
                rowW += BETWEEN_HINTS_GAP;
            }
            rowW += clusterWidth(hud, right.label(), rmbIcon.getRegionWidth());
        }

        float rowX = Constants.HUD_WIDTH_PX - MARGIN_RIGHT - rowW;
        float rowY = MARGIN_BOTTOM;
        BatchDraw.tintedRect(
                batch,
                assets.whitePixel,
                rowX - ROW_PAD,
                rowY - ROW_PAD,
                rowW + ROW_PAD * 2f,
                rowH + ROW_PAD * 2f,
                RenderColors.DROP_LABEL_BG);

        float x = rowX;
        if (right != null) {
            float w = clusterWidth(hud, right.label(), rmbIcon.getRegionWidth());
            drawCluster(
                    batch,
                    hud,
                    rmbIcon,
                    rmbIcon.getRegionWidth(),
                    rmbIcon.getRegionHeight(),
                    right.label(),
                    x,
                    rowY,
                    rowH);
            x += w;
            if (left != null) {
                x += BETWEEN_HINTS_GAP;
            }
        }
        if (left != null) {
            drawCluster(
                    batch,
                    hud,
                    lmbIcon,
                    lmbIcon.getRegionWidth(),
                    lmbIcon.getRegionHeight(),
                    left.label(),
                    x,
                    rowY,
                    rowH);
        }
    }

    private static void renderKeyHints(
            SpriteBatch batch,
            HudAssets hud,
            DawnAssets assets,
            boolean showDropHint,
            boolean showInventoryHint) {
        TextureRegion qKey = assets.uiCommon.qKey;
        TextureRegion eKey = assets.uiCommon.eKey;
        boolean canShowQ = showDropHint && qKey != null;
        boolean canShowE = showInventoryHint && eKey != null;
        if (!canShowQ && !canShowE) {
            return;
        }

        float qH = canShowQ ? qKey.getRegionHeight() : 0f;
        float eH = canShowE ? eKey.getRegionHeight() : 0f;
        float rowH = Math.max(Math.max(qH, eH), lineHeight(hud));
        float rowW = 0f;
        if (canShowQ) {
            rowW += clusterWidth(hud, "Drop", qKey.getRegionWidth());
        }
        if (canShowE) {
            if (rowW > 0f) {
                rowW += BETWEEN_HINTS_GAP;
            }
            rowW += clusterWidth(hud, "Inventory", eKey.getRegionWidth());
        }

        float rowX = MARGIN_LEFT;
        float rowY = MARGIN_BOTTOM;
        BatchDraw.tintedRect(
                batch,
                assets.whitePixel,
                rowX - ROW_PAD,
                rowY - ROW_PAD,
                rowW + ROW_PAD * 2f,
                rowH + ROW_PAD * 2f,
                RenderColors.DROP_LABEL_BG);

        float x = rowX;
        if (canShowQ) {
            float w = clusterWidth(hud, "Drop", qKey.getRegionWidth());
            drawCluster(
                    batch,
                    hud,
                    qKey,
                    qKey.getRegionWidth(),
                    qKey.getRegionHeight(),
                    "Drop",
                    x,
                    rowY,
                    rowH);
            x += w;
            if (canShowE) {
                x += BETWEEN_HINTS_GAP;
            }
        }
        if (canShowE) {
            drawCluster(
                    batch,
                    hud,
                    eKey,
                    eKey.getRegionWidth(),
                    eKey.getRegionHeight(),
                    "Inventory",
                    x,
                    rowY,
                    rowH);
        }
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
