package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EntityPixelSnapTest {

    @Test
    void forMovement_idleUsesDisplayGrid() {
        assertEquals(EntityPixelSnap.DISPLAY_GRID, EntityPixelSnap.forMovement(false, false, 0f, 0f));
    }

    @Test
    void forMovement_cardinalUsesAxisSmooth() {
        assertEquals(EntityPixelSnap.AXIS_SMOOTH, EntityPixelSnap.forMovement(false, true, 1f, 0f));
        assertEquals(EntityPixelSnap.AXIS_SMOOTH, EntityPixelSnap.forMovement(false, true, 0f, -1f));
    }

    @Test
    void forMovement_diagonalUsesDiagonalSnap() {
        assertEquals(EntityPixelSnap.DIAGONAL, EntityPixelSnap.forMovement(false, true, 0.707f, 0.707f));
    }

    @Test
    void snapAxisMonotonic_positiveMove_floorsWithoutBackwardSteps() {
        float a = PixelAlign.snapAxisMonotonic(10.2f, 1f);
        float b = PixelAlign.snapAxisMonotonic(10.8f, 1f);
        assertEquals(Math.floor(10.2f * 3) / 3f, a, 0.0001f);
        assertEquals(Math.floor(10.8f * 3) / 3f, b, 0.0001f);
        assertTrue(b >= a);
    }

    @Test
    void snapAxisMonotonic_negativeMove_ceilsWithoutForwardSteps() {
        float a = PixelAlign.snapAxisMonotonic(10.8f, -1f);
        float b = PixelAlign.snapAxisMonotonic(10.2f, -1f);
        assertEquals(Math.ceil(10.8f * 3) / 3f, a, 0.0001f);
        assertEquals(Math.ceil(10.2f * 3) / 3f, b, 0.0001f);
        assertTrue(b <= a);
    }
}
