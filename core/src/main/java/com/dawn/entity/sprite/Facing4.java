package com.dawn.entity.sprite;

/** Top-down four-way facing for idle, walk, and interact clips. */
public enum Facing4 {
    DOWN,
    UP,
    RIGHT,
    LEFT;

    public boolean flipX() {
        return this == LEFT;
    }

    public String idleClipId() {
        return switch (this) {
            case DOWN -> "idle_down";
            case UP -> "idle_up";
            case RIGHT, LEFT -> "idle_right";
        };
    }

    public String walkClipId() {
        return switch (this) {
            case DOWN -> "walk_down";
            case UP -> "walk_up";
            case RIGHT, LEFT -> "walk_right";
        };
    }

    public String interactClipId() {
        return switch (this) {
            case DOWN -> "interact_down";
            case UP -> "interact_up";
            case RIGHT, LEFT -> "interact_right";
        };
    }
}
