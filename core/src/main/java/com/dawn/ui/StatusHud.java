package com.dawn.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.entity.Entity;
import com.dawn.entity.status.StatusId;
import com.dawn.entity.status.StatusSet;

/** Top-left status icon strip rendered left-to-right in HUD space. */
public final class StatusHud {
    private static final float MARGIN_LEFT = 12f * Constants.HUD_ART_MULT / 2f;
    private static final float MARGIN_TOP = 12f * Constants.HUD_ART_MULT / 2f;
    private static final float ICON_SIZE = 16f * Constants.HUD_ART_MULT;
    private static final float ICON_GAP = 8f * Constants.HUD_ART_MULT / 2f;

    private static final StatusId[] ORDER = {
        StatusId.POISONED,
        StatusId.HUNGRY,
        StatusId.STARVING,
        StatusId.BURDENED,
        StatusId.IMMOBILE
    };

    private final HudAssets hud;
    private final DawnAssets assets;

    public StatusHud(HudAssets hud, DawnAssets assets) {
        this.hud = hud;
        this.assets = assets;
    }

    public void render(Entity player) {
        if (player == null) {
            return;
        }
        StatusSet statuses = player.getStatuses();
        if (statuses == null || statuses.isEmpty()) {
            return;
        }

        float x = MARGIN_LEFT;
        float y = Constants.HUD_HEIGHT_PX - MARGIN_TOP - ICON_SIZE;
        boolean any = false;
        hud.batch.begin();
        for (StatusId id : ORDER) {
            if (!statuses.has(id)) {
                continue;
            }
            TextureRegion icon = iconFor(id);
            if (icon == null) {
                continue;
            }
            any = true;
            hud.batch.draw(icon, x, y, ICON_SIZE, ICON_SIZE);
            x += ICON_SIZE + ICON_GAP;
        }
        hud.batch.end();
        if (!any) {
            return;
        }
    }

    private TextureRegion iconFor(StatusId id) {
        return switch (id) {
            case POISONED -> assets.uiCommon.statusPoison;
            case HUNGRY -> assets.uiCommon.statusHungry;
            case STARVING -> assets.uiCommon.statusStarving;
            case BURDENED -> assets.uiCommon.statusBurdened;
            case IMMOBILE -> assets.uiCommon.statusImmobile;
        };
    }
}
