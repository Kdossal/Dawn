package com.dawn.ui;

/** Shared item icon sizing inside slot chrome (hotbar is the reference). */
public final class SlotUi {
    public static final float SLOT_PX = Hotbar.SLOT_W;
    public static final float ICON_PX = 36f;

    private SlotUi() {}

    /** Icon draw size for a slot of {@code slotPx}, same fill ratio as the hotbar. */
    public static float iconPxForSlot(float slotPx) {
        return slotPx * ICON_PX / SLOT_PX;
    }
}
