package com.dawn.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class BatchDraw {
    private BatchDraw() {}

    public static void tintedRect(
            SpriteBatch batch, TextureRegion region, float x, float y, float w, float h, Color tint) {
        batch.setColor(tint.r, tint.g, tint.b, tint.a);
        batch.draw(region, x, y, w, h);
        batch.setColor(Color.WHITE);
    }
}
