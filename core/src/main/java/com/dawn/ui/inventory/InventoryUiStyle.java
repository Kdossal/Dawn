package com.dawn.ui.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.dawn.assets.DawnAssets;

public final class InventoryUiStyle {
    public static final float DIM_ALPHA = 0.45f;

    public static final Color LABEL_COLOR = new Color(0.85f, 0.87f, 0.9f, 1f);

    private InventoryUiStyle() {}

    public static TextureRegionDrawable fixedDrawable(TextureRegion region) {
        return new TextureRegionDrawable(region);
    }

    /** Compact tooltip panel (stretches; does not force chrome size). */
    public static Drawable tooltipBackground(DawnAssets assets) {
        TextureRegion white = assets.whitePixel;
        return new TextureRegionDrawable(white) {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                Color old = batch.getColor();
                batch.setColor(0.12f, 0.13f, 0.16f, 0.92f);
                super.draw(batch, x, y, width, height);
                batch.setColor(old);
            }
        };
    }
}
