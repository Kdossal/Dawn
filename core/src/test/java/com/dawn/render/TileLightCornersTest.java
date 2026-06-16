package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.render.TileLighting.TileLight;
import org.junit.jupiter.api.Test;

class TileLightCornersTest {
    @Test
    void uniform_setsAllCornersEqual() {
        TileLight center = new TileLight(0.6f, 0.7f, 0.8f);
        TileLightCorners corners = TileLightCorners.uniform(center);
        assertEquals(center, corners.bottomLeft());
        assertEquals(center, corners.bottomRight());
        assertEquals(center, corners.topLeft());
        assertEquals(center, corners.topRight());
    }

    @Test
    void fromCenterSample_matchesUniform() {
        TileLight center = new TileLight(0.4f, 0.5f, 0.6f);
        TileLightCorners corners = TileLightCorners.fromCenterSample(center);
        assertEquals(TileLightCorners.uniform(center), corners);
    }

    @Test
    void debugGradient_leftBrightRightDark() {
        TileLightCorners corners = TileLightCorners.debugGradient();
        assertEquals(1f, corners.bottomLeft().r(), 1e-5f);
        assertEquals(0.15f, corners.bottomRight().r(), 1e-5f);
        assertEquals(corners.bottomLeft(), corners.topLeft());
        assertEquals(corners.bottomRight(), corners.topRight());
    }
}
