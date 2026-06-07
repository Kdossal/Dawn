package com.dawn.input;

import com.dawn.config.GameConfig;

/**
 * Activates run on a second press of the same movement key within {@link GameConfig#runDoubleTapWindowSec}.
 * Run stays active while at least one movement key is held; releasing all movement keys clears it.
 */
public final class DoubleTapRunTracker {
    private int lastTapKey = -1;
    private double lastTapTimeSec = -1d;
    private boolean running;

    public void onKeyDown(int keycode, double timeSec) {
        if (!MovementKeys.isMovementKey(keycode)) {
            return;
        }
        GameConfig cfg = GameConfig.get();
        if (lastTapKey == keycode
                && lastTapTimeSec >= 0d
                && timeSec - lastTapTimeSec <= cfg.runDoubleTapWindowSec) {
            running = true;
        }
        lastTapKey = keycode;
        lastTapTimeSec = timeSec;
    }

    public void onKeyUp(int keycode, boolean anyMovementHeld) {
        if (!MovementKeys.isMovementKey(keycode)) {
            return;
        }
        if (!anyMovementHeld) {
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    /** Package-private for tests. */
    void reset() {
        lastTapKey = -1;
        lastTapTimeSec = -1d;
        running = false;
    }
}
