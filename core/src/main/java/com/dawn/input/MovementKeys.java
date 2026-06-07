package com.dawn.input;

import com.badlogic.gdx.Input;

/** WASD and arrow keys used for locomotion. */
public final class MovementKeys {
    private static final int[] ALL = {
        Input.Keys.W,
        Input.Keys.A,
        Input.Keys.S,
        Input.Keys.D,
        Input.Keys.UP,
        Input.Keys.DOWN,
        Input.Keys.LEFT,
        Input.Keys.RIGHT
    };

    private MovementKeys() {}

    public static boolean isMovementKey(int keycode) {
        for (int key : ALL) {
            if (key == keycode) {
                return true;
            }
        }
        return false;
    }

    public static int[] all() {
        return ALL;
    }
}
