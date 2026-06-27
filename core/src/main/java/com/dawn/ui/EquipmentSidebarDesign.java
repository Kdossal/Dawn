package com.dawn.ui;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import com.dawn.inventory.EquipmentSlot;

/** Art-base layout for the HUD equipment sidebar (scaled by {@link Constants#HUD_ART_MULT}). */
public final class EquipmentSidebarDesign {
    public static final float BASE_TAB_W = 10f;
    public static final float BASE_TAB_H = 16f;
    public static final float BASE_PANEL_W = 50f;
    public static final float BASE_PANEL_H = 116f;
    public static final float BASE_INSET = 4f;
    public static final float BASE_OFFHAND_GAP = 2f;
    public static final float BASE_TAB_ON_PANEL_INSET = 2f;

    public static final int SLOT_ROWS = 4;
    public static final int SLOT_COLS = 2;

    private EquipmentSidebarDesign() {}

    public record Layout(
            int multiplier,
            float tabW,
            float tabH,
            float panelW,
            float panelH,
            float slotPx,
            float iconPx,
            float inset,
            float slotGap,
            float offhandGap,
            float tabX,
            float tabY,
            float panelX,
            float panelY) {}

    public static Layout layout() {
        int mult = Constants.HUD_ART_MULT;
        float tabW = BASE_TAB_W * mult;
        float tabH = BASE_TAB_H * mult;
        float panelW = BASE_PANEL_W * mult;
        float panelH = BASE_PANEL_H * mult;
        float slotPx = HudSlotDesign.slotPx();
        float iconPx = SlotUi.iconPxForSlot(slotPx);
        float inset = BASE_INSET * mult;
        float slotGap = HudSlotDesign.gapPx();
        float offhandGap = BASE_OFFHAND_GAP * mult;
        float hudW = Constants.HUD_WIDTH_PX;
        float hudH = Constants.HUD_HEIGHT_PX;
        float tabX = hudW - tabW;
        float panelX = hudW - panelW;
        float panelY = hudH / 2f - panelH / 2f;
        float tabY = panelY + panelH / 2f - tabH / 2f;
        return new Layout(
                mult,
                tabW,
                tabH,
                panelW,
                panelH,
                slotPx,
                iconPx,
                inset,
                slotGap,
                offhandGap,
                tabX,
                tabY,
                panelX,
                panelY);
    }

    /** Tab anchor X while sliding: closed = screen edge; open = inset onto panel left edge. */
    public static float tabXAtSlide(float slideT, float panelX, Layout layout) {
        if (slideT <= 0.001f) {
            return layout.tabX();
        }
        return panelX + BASE_TAB_ON_PANEL_INSET * layout.multiplier();
    }

    /** Panel X while sliding: {@code slideT}=0 off-screen right, {@code slideT}=1 flush with HUD right edge. */
    public static float panelXAtSlide(float slideT, Layout layout) {
        float hudW = Constants.HUD_WIDTH_PX;
        return hudW - layout.panelW() * Math.max(0f, Math.min(1f, slideT));
    }

    /** Slot position for a panel at {@code panelX} (top row = armor head / accessory 1). */
    public static void slotBounds(float panelX, Layout layout, int col, int row, Rectangle out) {
        float x = panelX + layout.inset() + col * (layout.slotPx() + layout.slotGap());
        float y =
                layout.panelY()
                        + layout.panelH()
                        - layout.inset()
                        - layout.slotPx()
                        - row * (layout.slotPx() + layout.slotGap());
        out.set(x, y, layout.slotPx(), layout.slotPx());
    }

    /** Centered offhand slot below armor/accessory columns. */
    public static void offhandBounds(float panelX, Layout layout, Rectangle out) {
        float gridW = SLOT_COLS * layout.slotPx() + (SLOT_COLS - 1) * layout.slotGap();
        float x = panelX + layout.inset() + (gridW - layout.slotPx()) * 0.5f;
        float y = layout.panelY() + layout.inset();
        out.set(x, y, layout.slotPx(), layout.slotPx());
    }

    /** Inner grid size at 1× art (42×86). */
    public static float innerGridWidth() {
        return SLOT_COLS * HudSlotDesign.BASE_SLOT_PX + (SLOT_COLS - 1) * HudSlotDesign.BASE_GAP_PX;
    }

    public static float innerGridHeight() {
        return SLOT_ROWS * HudSlotDesign.BASE_SLOT_PX + (SLOT_ROWS - 1) * HudSlotDesign.BASE_GAP_PX;
    }

    public static EquipmentSlot slotAt(int col, int row) {
        if (col == 0) {
            return EquipmentSlot.wearOrder()[row];
        }
        return EquipmentSlot.accessoryOrder()[row];
    }
}
