package com.dawn.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.GameConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoubleTapRunTrackerTest {
    private static final int W = com.badlogic.gdx.Input.Keys.W;
    private static final int A = com.badlogic.gdx.Input.Keys.A;

    private float savedWindow;

    @BeforeEach
    void saveConfig() {
        savedWindow = GameConfig.get().runDoubleTapWindowSec;
        GameConfig.get().runDoubleTapWindowSec = 0.3f;
    }

    @AfterEach
    void restoreConfig() {
        GameConfig.get().runDoubleTapWindowSec = savedWindow;
    }

    @Test
    void singleTapDoesNotRun() {
        DoubleTapRunTracker tracker = new DoubleTapRunTracker();
        tracker.onKeyDown(W, 1.0);
        assertFalse(tracker.isRunning());
    }

    @Test
    void doubleTapSameKeyWithinWindowStartsRun() {
        DoubleTapRunTracker tracker = new DoubleTapRunTracker();
        tracker.onKeyDown(W, 1.0);
        tracker.onKeyDown(W, 1.15);
        assertTrue(tracker.isRunning());
    }

    @Test
    void doubleTapOutsideWindowDoesNotRun() {
        DoubleTapRunTracker tracker = new DoubleTapRunTracker();
        tracker.onKeyDown(W, 1.0);
        tracker.onKeyDown(W, 1.5);
        assertFalse(tracker.isRunning());
    }

    @Test
    void differentKeysDoNotStartRun() {
        DoubleTapRunTracker tracker = new DoubleTapRunTracker();
        tracker.onKeyDown(W, 1.0);
        tracker.onKeyDown(A, 1.1);
        assertFalse(tracker.isRunning());
    }

    @Test
    void releasingAllMovementKeysClearsRun() {
        DoubleTapRunTracker tracker = new DoubleTapRunTracker();
        tracker.onKeyDown(W, 1.0);
        tracker.onKeyDown(W, 1.1);
        assertTrue(tracker.isRunning());
        tracker.onKeyUp(W, false);
        assertFalse(tracker.isRunning());
    }

    @Test
    void releasingOneKeyWhileOthersHeldKeepsRun() {
        DoubleTapRunTracker tracker = new DoubleTapRunTracker();
        tracker.onKeyDown(W, 1.0);
        tracker.onKeyDown(W, 1.1);
        tracker.onKeyUp(W, true);
        assertTrue(tracker.isRunning());
    }
}
