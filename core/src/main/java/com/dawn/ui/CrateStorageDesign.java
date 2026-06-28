package com.dawn.ui;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import com.dawn.world.storage.CrateStorage;

/** Art-base layout for the in-world crate storage panel (4×3, scaled by {@link Constants#HUD_ART_MULT}). */
public final class CrateStorageDesign {
    public static final int SLOT_COLS = CrateStorage.COLS;
    public static final int SLOT_ROWS = CrateStorage.ROWS;

    public static final float BASE_INSET = HudPanelDesign.BASE_INSET;

    private CrateStorageDesign() {}

    public record Layout(
            float panelW,
            float panelH,
            float slotPx,
            float iconPx,
            float inset,
            float slotGap) {}

    public static Layout layout() {
        int mult = Constants.HUD_ART_MULT;
        float slotPx = HudSlotDesign.slotPx();
        float iconPx = SlotUi.iconPxForSlot(slotPx);
        float inset = BASE_INSET * mult;
        float slotGap = HudSlotDesign.gapPx();
        float panelW = inset * 2f + SLOT_COLS * slotPx + (SLOT_COLS - 1) * slotGap;
        float panelH = inset * 2f + SLOT_ROWS * slotPx + (SLOT_ROWS - 1) * slotGap;
        return new Layout(panelW, panelH, slotPx, iconPx, inset, slotGap);
    }

    public static void slotBounds(float panelX, float panelY, Layout layout, int col, int row, Rectangle out) {
        float x = panelX + layout.inset() + col * (layout.slotPx() + layout.slotGap());
        float y =
                panelY
                        + layout.panelH()
                        - layout.inset()
                        - layout.slotPx()
                        - row * (layout.slotPx() + layout.slotGap());
        out.set(x, y, layout.slotPx(), layout.slotPx());
    }
}
