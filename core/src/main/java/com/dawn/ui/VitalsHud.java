package com.dawn.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.dawn.assets.DawnAssets;
import com.dawn.entity.Entity;
import com.dawn.render.GameSettings;

/** Top-right HP/EP bars rendered in HUD batch space (no stretching). */
public final class VitalsHud {
    private static final float FULL_HIDE_DELAY_SEC = 3.0f;
    // Product decision: hunger/thirst symbols will be status effects for now.
    // Keep bar implementation in code for an easy rollback; rendering is intentionally disabled.
    private static final boolean SHOW_HUNGER_THIRST_BARS = false;

    private final HudAssets hud;
    private final DawnAssets assets;
    private final GameSettings settings;

    private final TextureRegion emptyClip = new TextureRegion();
    private final TextureRegion fillClip = new TextureRegion();
    private final TextureRegion smallEmptyClip = new TextureRegion();
    private final TextureRegion smallFillClip = new TextureRegion();

    private float hpFullForSec;
    private float epFullForSec;
    private float hungerFullForSec;
    private float thirstFullForSec;

    public VitalsHud(HudAssets hud, DawnAssets assets, GameSettings settings) {
        this.hud = hud;
        this.assets = assets;
        this.settings = settings;
    }

    public void render(Entity player) {
        if (player == null) {
            return;
        }

        VitalsBarDesign.Layout layout = VitalsBarDesign.layout(GameSettings.UiSize.MEDIUM);
        float hpCurrent = player.getCurrentHp();
        float hpMax = player.getMaxHp();
        float epCurrent = player.getCurrentEnergy();
        float epMax = player.getMaxEnergy();
        float delta = Gdx.graphics.getDeltaTime();

        hud.batch.begin();
        if (shouldRenderRow(hpCurrent, hpMax, delta, RowId.HP)) {
            drawRow(layout, VitalsBarDesign.Row.HP, 0, hpCurrent, hpMax, assets.uiCommon.hpFill, assets.uiCommon.hpIcon);
        }
        if (shouldRenderRow(epCurrent, epMax, delta, RowId.EP)) {
            drawRow(layout, VitalsBarDesign.Row.EP, 1, epCurrent, epMax, assets.uiCommon.epFill, assets.uiCommon.epIcon);
        }
        if (SHOW_HUNGER_THIRST_BARS) {
            float hungerCurrent = player.getCurrentHunger();
            float thirstCurrent = player.getCurrentThirst();
            if (shouldRenderRow(hungerCurrent, 100f, delta, RowId.HUNGER)) {
                drawSmallRow(layout, 2, hungerCurrent, assets.uiCommon.hungerFill, assets.uiCommon.hungerIcon);
            }
            if (shouldRenderRow(thirstCurrent, 100f, delta, RowId.THIRST)) {
                drawSmallRow(layout, 3, thirstCurrent, assets.uiCommon.thirstFill, assets.uiCommon.thirstIcon);
            }
        }
        hud.batch.end();
    }

    private enum RowId {
        HP,
        EP,
        HUNGER,
        THIRST
    }

    private boolean shouldRenderRow(float current, float max, float delta, RowId id) {
        if (!VitalsBarDesign.isFull(current, max)) {
            setFullTimer(id, 0f);
            return true;
        }
        float t = getFullTimer(id) + Math.max(0f, delta);
        setFullTimer(id, t);
        return t < FULL_HIDE_DELAY_SEC;
    }

    private float getFullTimer(RowId id) {
        return switch (id) {
            case HP -> hpFullForSec;
            case EP -> epFullForSec;
            case HUNGER -> hungerFullForSec;
            case THIRST -> thirstFullForSec;
        };
    }

    private void setFullTimer(RowId id, float value) {
        switch (id) {
            case HP -> hpFullForSec = value;
            case EP -> epFullForSec = value;
            case HUNGER -> hungerFullForSec = value;
            case THIRST -> thirstFullForSec = value;
        }
    }

    private void drawRow(
            VitalsBarDesign.Layout layout,
            VitalsBarDesign.Row row,
            int rowIndex,
            float currentValue,
            float maxValue,
            TextureRegion fillTexture,
            TextureRegion iconTexture) {
        int bodyPxAtOneX = VitalsBarDesign.bodyPxAtOneX(maxValue);
        int fillPxAtOneX = VitalsBarDesign.fillPxAtOneX(currentValue, maxValue);
        VitalsBarDesign.RowLayout rowLayout = VitalsBarDesign.rowLayout(layout, row, rowIndex, bodyPxAtOneX);

        int srcTrackW = VitalsBarDesign.BASE_LEFT_CAP_W + bodyPxAtOneX;
        emptyClip.setRegion(assets.uiCommon.emptyBar, 0, 0, srcTrackW, VitalsBarDesign.BASE_EMPTY_H);
        hud.batch.draw(
                emptyClip,
                rowLayout.barX(),
                rowLayout.barY(),
                srcTrackW * layout.multiplier(),
                VitalsBarDesign.BASE_EMPTY_H * layout.multiplier());

        if (fillPxAtOneX > 0) {
            int srcFillW = Math.min(fillPxAtOneX, fillTexture.getRegionWidth());
            int srcFillX = fillTexture.getRegionWidth() - srcFillW;
            fillClip.setRegion(fillTexture, srcFillX, 0, srcFillW, VitalsBarDesign.BASE_FILL_H);

            int fillDestW = srcFillW * layout.multiplier();
            int fillAreaX = rowLayout.barX() + layout.fillInsetX();
            int fillAreaW = bodyPxAtOneX * layout.multiplier();
            int fillRight = fillAreaX + fillAreaW;
            int fillDestX = fillRight - fillDestW;
            hud.batch.draw(fillClip, fillDestX, rowLayout.barY() + layout.fillInsetY(), fillDestW, layout.fillH());
        }

        int iconW = row == VitalsBarDesign.Row.HP ? layout.hpIconW() : layout.epIconW();
        hud.batch.draw(iconTexture, rowLayout.iconX(), rowLayout.iconY(), iconW, layout.iconH());
    }

    private void drawSmallRow(
            VitalsBarDesign.Layout layout,
            int rowIndex,
            float valuePercent,
            TextureRegion fillTexture,
            TextureRegion iconTexture) {
        int fillPxAtOneX = VitalsBarDesign.smallFillPxAtOneX(valuePercent);
        VitalsBarDesign.RowLayout row = VitalsBarDesign.smallRowLayout(layout, rowIndex);

        smallEmptyClip.setRegion(
                assets.uiCommon.smallEmptyBar, 0, 0, VitalsBarDesign.BASE_SMALL_EMPTY_W, VitalsBarDesign.BASE_SMALL_EMPTY_H);
        hud.batch.draw(
                smallEmptyClip,
                row.barX(),
                row.barY(),
                VitalsBarDesign.scaledSmallBarWidth(layout),
                VitalsBarDesign.BASE_SMALL_EMPTY_H * layout.multiplier());

        if (fillPxAtOneX > 0) {
            int srcFillW = Math.min(fillPxAtOneX, fillTexture.getRegionWidth());
            int srcFillX = fillTexture.getRegionWidth() - srcFillW;
            smallFillClip.setRegion(fillTexture, srcFillX, 0, srcFillW, VitalsBarDesign.BASE_SMALL_FILL_H);

            int fillDestW = srcFillW * layout.multiplier();
            int fillAreaX = row.barX() + layout.smallFillInsetX();
            int fillAreaW = VitalsBarDesign.BASE_SMALL_FILL_W * layout.multiplier();
            int fillRight = fillAreaX + fillAreaW;
            int fillDestX = fillRight - fillDestW;
            hud.batch.draw(
                    smallFillClip,
                    fillDestX,
                    row.barY() + layout.smallFillInsetY(),
                    fillDestW,
                    layout.smallFillH());
        }

        hud.batch.draw(iconTexture, row.iconX(), row.iconY(), layout.smallIconW(), layout.smallIconH());
    }
}
