package com.dawn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.Color;
import com.dawn.render.TileLighting.TileLight;
import org.junit.jupiter.api.Test;

class LitQuadDrawTest {
    @Test
    void buildVertices_placesCornersInSpriteBatchOrder() {
        TileLight left = new TileLight(1f, 0.85f, 0.7f);
        TileLight right = new TileLight(0.15f, 0.15f, 0.18f);
        TileLightCorners corners = new TileLightCorners(left, right, left, right);

        float[] vertices =
                LitQuadDraw.buildVertices(0.1f, 0.2f, 0.9f, 0.8f, 10f, 20f, 32f, 64f, corners, 0.5f, false);

        assertEquals(10f, vertices[0], 1e-5f);
        assertEquals(20f, vertices[1], 1e-5f);
        assertColor(vertices[2], left, 0.5f);

        assertEquals(10f, vertices[5], 1e-5f);
        assertEquals(84f, vertices[6], 1e-5f);
        assertColor(vertices[7], left, 0.5f);

        assertEquals(42f, vertices[10], 1e-5f);
        assertEquals(84f, vertices[11], 1e-5f);
        assertColor(vertices[12], right, 0.5f);

        assertEquals(42f, vertices[15], 1e-5f);
        assertEquals(20f, vertices[16], 1e-5f);
        assertColor(vertices[17], right, 0.5f);

        assertEquals(0.1f, vertices[3], 1e-5f);
        assertEquals(0.8f, vertices[4], 1e-5f);
        assertEquals(0.1f, vertices[8], 1e-5f);
        assertEquals(0.2f, vertices[9], 1e-5f);
        assertEquals(0.9f, vertices[13], 1e-5f);
        assertEquals(0.2f, vertices[14], 1e-5f);
        assertEquals(0.9f, vertices[18], 1e-5f);
        assertEquals(0.8f, vertices[19], 1e-5f);
    }

    @Test
    void buildVertices_flipX_swapsTextureU() {
        TileLight light = TileLight.fullWhite();
        TileLightCorners corners = TileLightCorners.uniform(light);
        float[] vertices =
                LitQuadDraw.buildVertices(0.1f, 0.2f, 0.9f, 0.8f, 0f, 0f, 16f, 16f, corners, 1f, true);

        assertEquals(0.9f, vertices[3], 1e-5f);
        assertEquals(0.9f, vertices[8], 1e-5f);
        assertEquals(0.1f, vertices[13], 1e-5f);
        assertEquals(0.1f, vertices[18], 1e-5f);
    }

    private static void assertColor(float bits, TileLight light, float alpha) {
        assertEquals(Color.toFloatBits(light.r(), light.g(), light.b(), alpha), bits, 0f);
    }
}
