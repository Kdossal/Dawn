package com.dawn.gameplay;

/** HUD label for a mouse button action (combat not implemented for ATTACK yet). */
public enum ClickVerb {
    ATTACK,
    GRAB,
    DIG,
    MINE,
    CHOP,
    PLACE;

    public String label() {
        return switch (this) {
            case ATTACK -> "Attack";
            case GRAB -> "Grab";
            case DIG -> "Dig";
            case MINE -> "Mine";
            case CHOP -> "Chop";
            case PLACE -> "Place";
        };
    }
}
