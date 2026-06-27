package com.dawn.ui;

/** Slot background variant for HUD item slots. */
public enum HudSlotChrome {
    /** Hotbar: {@code slot} / {@code slotSelected} when selected. */
    HOTBAR,
    /** Sidebar, crate, equipment: dim {@code slotDull}. */
    DULL,
    /** Drag cursor: icon and stack count only (no slot chrome). */
    FLOATING
}
