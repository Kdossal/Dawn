package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DawnFontsTest {
    @Test
    void nativePointSizeIsSixteen() {
        assertEquals(16, DawnFonts.NATIVE_POINT_SIZE);
    }
}
