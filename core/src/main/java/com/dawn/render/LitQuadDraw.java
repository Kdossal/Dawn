package com.dawn.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.render.TileLighting.TileLight;

/** Draws a textured quad with four corner RGB tints via SpriteBatch vertex colors. */
public final class LitQuadDraw {
    static final int VERTEX_COUNT = 20;
    static final int FLOATS_PER_VERTEX = 5;

    private LitQuadDraw() {}

    public static void drawRegion(
            SpriteBatch batch,
            TextureRegion region,
            float x,
            float y,
            float w,
            float h,
            TileLightCorners corners,
            float alpha,
            boolean flipX) {
        if (region == null) {
            return;
        }
        float[] vertices =
                buildVertices(
                        region.getU(),
                        region.getV(),
                        region.getU2(),
                        region.getV2(),
                        x,
                        y,
                        w,
                        h,
                        corners,
                        alpha,
                        flipX);
        batch.draw(region.getTexture(), vertices, 0, VERTEX_COUNT);
        batch.setColor(Color.WHITE);
    }

    static float[] buildVertices(
            float u,
            float v,
            float u2,
            float v2,
            float x,
            float y,
            float w,
            float h,
            TileLightCorners corners,
            float alpha,
            boolean flipX) {
        float leftU = flipX ? u2 : u;
        float rightU = flipX ? u : u2;

        float[] vertices = new float[VERTEX_COUNT];
        // SpriteBatch vertex order: BL, TL, TR, BR.
        // TextureRegion UVs are y-down: v is top, v2 is bottom.
        putVertex(vertices, 0, x, y, corners.bottomLeft(), alpha, leftU, v2);
        putVertex(vertices, 5, x, y + h, corners.topLeft(), alpha, leftU, v);
        putVertex(vertices, 10, x + w, y + h, corners.topRight(), alpha, rightU, v);
        putVertex(vertices, 15, x + w, y, corners.bottomRight(), alpha, rightU, v2);
        return vertices;
    }

    static float[] buildVertices(
            TextureRegion region,
            float x,
            float y,
            float w,
            float h,
            TileLightCorners corners,
            float alpha,
            boolean flipX) {
        return buildVertices(
                region.getU(),
                region.getV(),
                region.getU2(),
                region.getV2(),
                x,
                y,
                w,
                h,
                corners,
                alpha,
                flipX);
    }

    private static void putVertex(
            float[] vertices, int offset, float px, float py, TileLight light, float alpha, float u, float v) {
        vertices[offset] = px;
        vertices[offset + 1] = py;
        vertices[offset + 2] = Color.toFloatBits(light.r(), light.g(), light.b(), alpha);
        vertices[offset + 3] = u;
        vertices[offset + 4] = v;
    }
}
