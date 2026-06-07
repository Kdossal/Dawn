package com.dawn.entity.sprite;

import com.dawn.gameplay.TargetResolver.TargetCell;

/** Per-frame input for player animation selection. */
public record PlayerAnimContext(
        boolean moving,
        boolean interacting,
        float feetX,
        float feetY,
        float moveX,
        float moveY,
        TargetCell target) {

    public static PlayerAnimContext idle(float feetX, float feetY) {
        return new PlayerAnimContext(false, false, feetX, feetY, 0f, 0f, null);
    }
}
