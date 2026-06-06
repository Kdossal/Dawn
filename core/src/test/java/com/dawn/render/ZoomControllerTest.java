package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZoomControllerTest {
    @Test
    void displayedZoomApproachesTargetAfterUpdate() {
        GameSettings settings = new GameSettings();
        ZoomController controller = new ZoomController(settings);

        settings.setZoomLevel(10);
        for (int i = 0; i < 120; i++) {
            controller.update(0.016f);
        }

        assertTrue(Math.abs(controller.displayedZoomFactor() - settings.targetZoomFactor()) < 0.01f);
    }
}
