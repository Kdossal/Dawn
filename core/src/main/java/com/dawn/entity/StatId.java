package com.dawn.entity;

public enum StatId {
    VIGOR,
    STRENGTH,
    AGILITY,
    FOCUS,
    INTELLIGENCE,
    SURVIVAL;

    public static final StatId[] ALL = values();

    public static final String[] DISPLAY_NAMES = {
        "Vigor", "Strength", "Agility", "Focus", "Intelligence", "Survival"
    };
}
