package com.dawn.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/** Shared HUD drawing resources. */
public final class HudAssets implements Disposable {
    public final DawnFonts fonts;
    public final BitmapFont font;
    public final SpriteBatch batch;
    public final ShapeRenderer shapes;
    public final GlyphLayout layout;

    public HudAssets(DawnFonts fonts) {
        this.fonts = fonts;
        font = fonts.regular();
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        layout = new GlyphLayout();
    }

    public void setProjection(Matrix4 matrix) {
        batch.setProjectionMatrix(matrix);
        shapes.setProjectionMatrix(matrix);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
    }
}
