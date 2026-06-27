package com.dawn.ui;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import com.dawn.inventory.InventoryConstants;

/**
 * Art-base layout for HUD item slots (hotbar, equipment sidebar, crate storage).
 *
 * <p>Canonical slot/icon sizes for all in-world HUD drag-drop UI. Sidebar and crate panels reuse these
 * constants via {@link SlotUi#iconPxForSlot(float)}; only panel-specific inset/gap chrome differs.
 *
 * <p>See {@code docs/HUD_SCALING.md} for DISPLAY_SCALE / HUD_ART_MULT architecture. Inventory overlay
 * ({@code InventoryDesign}) is a separate coordinate system until redo — see {@code docs/INVENTORY_REDO.md}.
 */
public final class HudSlotDesign {
    public static final int SLOT_COUNT = InventoryConstants.SIZE;

    /** 1× PNG slot frame size (× {@link Constants#HUD_ART_MULT} on screen). */
    public static final float BASE_SLOT_PX = 20f;
    /** 1× item icon draw size inside a slot (× {@link Constants#HUD_ART_MULT} on screen). */
    public static final float BASE_ICON_PX = 16f;
    public static final float BASE_GAP_PX = 2f;
    /** Stack-count label inset from slot bottom-right (× {@link Constants#HUD_ART_MULT}). */
    public static final float BASE_EDGE_PAD = 2.5f;
    public static final float BASE_BOTTOM_MARGIN = 4f;

    private HudSlotDesign() {}

    public static int artMult() {
        return Constants.HUD_ART_MULT;
    }

    public static float slotPx() {
        return BASE_SLOT_PX * artMult();
    }

    public static float iconPx() {
        return BASE_ICON_PX * artMult();
    }

    public static float gapPx() {
        return BASE_GAP_PX * artMult();
    }

    public static float countPadPx() {
        return BASE_EDGE_PAD * artMult();
    }

    public static float bottomMarginPx() {
        return BASE_BOTTOM_MARGIN * artMult();
    }

    public static float barWidth() {
        return SLOT_COUNT * slotPx() + (SLOT_COUNT - 1) * gapPx();
    }

    public static float barOriginX() {
        return (Constants.HUD_WIDTH_PX - barWidth()) / 2f;
    }

    public static float barOriginY() {
        return bottomMarginPx();
    }

    public static float slotX(int index) {
        return barOriginX() + index * (slotPx() + gapPx());
    }

    public static void slotBounds(int index, Rectangle out) {
        out.set(slotX(index), barOriginY(), slotPx(), slotPx());
    }

    public static void barBounds(Rectangle out) {
        out.set(barOriginX(), barOriginY(), barWidth(), slotPx());
    }
}
