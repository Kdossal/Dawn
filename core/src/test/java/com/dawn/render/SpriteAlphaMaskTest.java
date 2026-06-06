package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpriteAlphaMaskTest {
    @Test
    void opaqueOverlap_falseWhenRectsTouchTransparentRegions() {
        SpriteAlphaMask left = SpriteAlphaMask.of(
                2,
                2,
                new boolean[] {true, false, true, false});
        SpriteAlphaMask right = SpriteAlphaMask.of(
                2,
                2,
                new boolean[] {false, true, false, true});

        assertFalse(SpriteAlphaMask.opaqueOverlap(left, 0f, 0f, right, 1f, 0f));
    }

    @Test
    void opaqueOverlap_trueWhenSolidPixelsAlign() {
        SpriteAlphaMask a = SpriteAlphaMask.of(1, 1, new boolean[] {true});
        SpriteAlphaMask b = SpriteAlphaMask.of(1, 1, new boolean[] {true});

        assertTrue(SpriteAlphaMask.opaqueOverlap(a, 10f, 10f, b, 10f, 10f));
    }
}
