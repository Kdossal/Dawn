package com.dawn.game;

import com.badlogic.gdx.math.Vector3;
import com.dawn.gameplay.InteractResolver.InteractTarget;
import com.dawn.gameplay.TargetResolver.TargetCell;

/** Per-frame transient and cross-frame presentation state for {@link GameScreen}. */
final class FrameState {
    final Vector3 mouseWorld = new Vector3();
    float lastMoveX;
    float lastMoveY;
    TargetCell target;
    InteractTarget interactTarget;

    boolean inventoryOpen;
    boolean overHotbar;
    boolean moving;
    boolean interacting;
    boolean paused;
}
