package com.dawn.entity;

/** Persistent RPG attributes (base + modifiers). Player defaults 10, cap 20. */
public enum AttributeId {
    VITALITY,
    BRAWN,
    AGILITY,
    FOCUS,
    INTELLECT,
    ARCANA;

    public static final AttributeId[] ALL = values();

    public static final String[] DISPLAY_NAMES = {
        "Vitality", "Brawn", "Agility", "Focus", "Intellect", "Arcana"
    };

    public static final String[] DEBUG_ABBREV = {"VIT", "BRN", "AGI", "FOC", "INT", "ARC"};

    public static final int PLAYER_MIN = 1;
    public static final int PLAYER_MAX = 20;
}
