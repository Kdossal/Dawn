package com.dawn.entity.sprite;

/** Side-view facing; art faces left by default, right is drawn by flipping on X. */
public enum Facing2 {
    LEFT,
    RIGHT;

    public boolean flipX() {
        return this == RIGHT;
    }

    public String idleClipId() {
        return "idle";
    }

    public String walkClipId() {
        return "walk";
    }

    public String interactClipId() {
        return "interact";
    }
}
