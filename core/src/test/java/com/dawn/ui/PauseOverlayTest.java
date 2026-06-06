package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PauseOverlayTest {
    @Test
    void pauseUnpauseAndToggle() {
        PauseOverlay overlay = PauseOverlay.forStateTest();

        assertFalse(overlay.isPaused());

        overlay.pause();
        assertTrue(overlay.isPaused());

        overlay.unpause();
        assertFalse(overlay.isPaused());

        overlay.toggle();
        assertTrue(overlay.isPaused());

        overlay.toggle();
        assertFalse(overlay.isPaused());
    }
}
