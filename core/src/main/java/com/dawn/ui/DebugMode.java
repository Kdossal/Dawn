package com.dawn.ui;

/** F3 debug overlay: OFF → stats/text → stats + world hitboxes → OFF. */
public enum DebugMode {
    OFF,
    STATS,
    STATS_WORLD;

    public DebugMode next() {
        return switch (this) {
            case OFF -> STATS;
            case STATS -> STATS_WORLD;
            case STATS_WORLD -> OFF;
        };
    }

    public boolean showsHudDebug() {
        return this == STATS || this == STATS_WORLD;
    }

    public boolean showsWorldDebug() {
        return this == STATS_WORLD;
    }
}
