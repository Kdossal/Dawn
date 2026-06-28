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
    private static final float HINT_SCALE = Constants.DISPLAY_SCALE / 2f;
    private static final float MARGIN_RIGHT = 12f * HINT_SCALE;
    private static final float MARGIN_LEFT = 12f * HINT_SCALE;
    private static final float MARGIN_BOTTOM = (22f - 2f) * HINT_SCALE;
    private static final float ICON_TEXT_GAP = 4f * HINT_SCALE;
    private static final float BETWEEN_HINTS_GAP = 12f * HINT_SCALE;
    private static final float ROW_PAD = 4f * HINT_SCALE;
    private static final TextTier TEXT_TIER = TextTier.SM;
    private static final Color TEXT_COLOR = InventoryUiStyle.LABEL_COLOR;

    private ClickHintRenderer() {}

    public static void render(
            HudAssets hud,
            DawnAssets assets,
            ClickHints hints,
            boolean showDropHint,
            boolean showCraftHint,
            boolean showInventoryHint,
            boolean showInteractHint) {
        SpriteBatch batch = hud.batch;
        batch.begin();
        renderClickHints(batch, hud, assets, hints);
        renderKeyHints(batch, hud, assets, showDropHint, showCraftHint, showInventoryHint, showInteractHint);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    /** Whether the inventory (I) hint should appear given E/C priority. */
    static boolean showInventoryKeyHint(boolean showInventoryHint, boolean showInteractHint) {
        return showInventoryHint && !showInteractHint;
    }

    private static float scaledIconW(float nativeW) {
        return nativeW * HINT_SCALE;
    }

    private static float scaledIconH(float nativeH) {
        return nativeH * HINT_SCALE;
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

        float lmbW = scaledIconW(lmbIcon.getRegionWidth());
        float lmbH = scaledIconH(lmbIcon.getRegionHeight());
        float rmbW = scaledIconW(rmbIcon.getRegionWidth());
        float rmbH = scaledIconH(rmbIcon.getRegionHeight());
        float rowH = Math.max(Math.max(lmbH, rmbH), lineHeight(hud));
        float rowW = 0f;
        if (left != null) {
            rowW += clusterWidth(hud, left.label(), lmbW);
        }
        if (right != null) {
            if (rowW > 0f) {
                rowW += BETWEEN_HINTS_GAP;
            }
            rowW += clusterWidth(hud, right.label(), rmbW);
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
            float w = clusterWidth(hud, right.label(), rmbW);
            drawCluster(batch, hud, rmbIcon, rmbW, rmbH, right.label(), x, rowY, rowH);
            x += w;
            if (left != null) {
                x += BETWEEN_HINTS_GAP;
            }
        }
        if (left != null) {
            drawCluster(batch, hud, lmbIcon, lmbW, lmbH, left.label(), x, rowY, rowH);
        }
    }

    private static void renderKeyHints(
            SpriteBatch batch,
            HudAssets hud,
            DawnAssets assets,
            boolean showDropHint,
            boolean showCraftHint,
            boolean showInventoryHint,
            boolean showInteractHint) {
        TextureRegion qKey = assets.uiCommon.qKey;
        TextureRegion eKey = assets.uiCommon.eKey;
        TextureRegion cKey = assets.uiCommon.cKey;
        TextureRegion iKey = assets.uiCommon.iKey;
        boolean canShowQ = showDropHint && qKey != null;
        boolean canShowE = showInteractHint && eKey != null;
        boolean canShowC = showCraftHint && cKey != null;
        boolean canShowI = showInventoryKeyHint(showInventoryHint, showInteractHint) && iKey != null;
        if (!canShowQ && !canShowE && !canShowC && !canShowI) {
            return;
        }

        float qW = canShowQ ? scaledIconW(qKey.getRegionWidth()) : 0f;
        float qH = canShowQ ? scaledIconH(qKey.getRegionHeight()) : 0f;
        float eW = canShowE ? scaledIconW(eKey.getRegionWidth()) : 0f;
        float eH = canShowE ? scaledIconH(eKey.getRegionHeight()) : 0f;
        float cW = canShowC ? scaledIconW(cKey.getRegionWidth()) : 0f;
        float cH = canShowC ? scaledIconH(cKey.getRegionHeight()) : 0f;
        float iW = canShowI ? scaledIconW(iKey.getRegionWidth()) : 0f;
        float iH = canShowI ? scaledIconH(iKey.getRegionHeight()) : 0f;
        float rowH = Math.max(Math.max(qH, Math.max(eH, Math.max(cH, iH))), lineHeight(hud));
        float rowW = 0f;
        if (canShowQ) {
            rowW += clusterWidth(hud, "Drop", qW);
        }
        if (canShowE) {
            if (rowW > 0f) {
                rowW += BETWEEN_HINTS_GAP;
            }
            rowW += clusterWidth(hud, "Interact", eW);
        }
        if (canShowC) {
            if (rowW > 0f) {
                rowW += BETWEEN_HINTS_GAP;
            }
            rowW += clusterWidth(hud, "Craft", cW);
        }
        if (canShowI) {
            if (rowW > 0f) {
                rowW += BETWEEN_HINTS_GAP;
            }
            rowW += clusterWidth(hud, "Inventory", iW);
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
            float w = clusterWidth(hud, "Drop", qW);
            drawCluster(batch, hud, qKey, qW, qH, "Drop", x, rowY, rowH);
            x += w;
            if (canShowE || canShowC || canShowI) {
                x += BETWEEN_HINTS_GAP;
            }
        }
        if (canShowE) {
            float w = clusterWidth(hud, "Interact", eW);
            drawCluster(batch, hud, eKey, eW, eH, "Interact", x, rowY, rowH);
            x += w;
            if (canShowC || canShowI) {
                x += BETWEEN_HINTS_GAP;
            }
        }
        if (canShowC) {
            float w = clusterWidth(hud, "Craft", cW);
            drawCluster(batch, hud, cKey, cW, cH, "Craft", x, rowY, rowH);
            x += w;
            if (canShowI) {
                x += BETWEEN_HINTS_GAP;
            }
        }
        if (canShowI) {
            drawCluster(batch, hud, iKey, iW, iH, "Inventory", x, rowY, rowH);
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
                hud.fonts,
                hud.layout,
                label,
                TEXT_TIER,
                TextContext.HUD,
                x + iconW + ICON_TEXT_GAP,
                rowY + (rowH + lineHeight(hud)) / 2f - 2f * HINT_SCALE,
                TEXT_COLOR);
    }

    private static float clusterWidth(HudAssets hud, String label, float iconW) {
        DawnTypography.layout(hud.layout, hud.fonts, label, TEXT_TIER, TextContext.HUD);
        return iconW + ICON_TEXT_GAP + hud.layout.width;
    }

    private static float lineHeight(HudAssets hud) {
        DawnTypography.layout(hud.layout, hud.fonts, "Ay", TEXT_TIER, TextContext.HUD);
        return hud.layout.height;
    }
}
