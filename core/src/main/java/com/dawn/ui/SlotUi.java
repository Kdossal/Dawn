package com.dawn.ui;

/** Shared item icon sizing inside slot chrome (HUD hotbar is the reference). */
public final class SlotUi {
    private SlotUi() {}

    /** Icon draw size for a slot of {@code slotPx}, same fill ratio as {@link HudSlotDesign}. */
    public static float iconPxForSlot(float slotPx) {
        return slotPx * HudSlotDesign.BASE_ICON_PX / HudSlotDesign.BASE_SLOT_PX;
    }
}
