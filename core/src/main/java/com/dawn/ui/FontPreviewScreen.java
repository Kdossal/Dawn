package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dawn.config.Constants;

/**
 * Smoke-test m5x7 at its native raster size (16pt → 5×7 px glyphs). Only integer scales from there.
 */
public final class FontPreviewScreen implements Screen, Disposable {
    private static final Color BG = new Color(0.08f, 0.09f, 0.12f, 1f);
    private static final Color LABEL_COLOR = new Color(0.92f, 0.94f, 0.97f, 1f);
    private static final Color DIM = new Color(0.55f, 0.58f, 0.65f, 1f);
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String SAMPLE = "DAWN — hotbar / inventory / pause";

    private final DawnFonts fonts;
    private final SpriteBatch batch;
    private final GlyphLayout layout;
    private final Stage stage;
    private final float lineHeightPx;

    public FontPreviewScreen() {
        fonts = new DawnFonts();
        lineHeightPx = fonts.lineHeightPx();
        batch = new SpriteBatch();
        layout = new GlyphLayout();
        stage = new Stage(new FitViewport(Constants.HUD_WIDTH_PX, Constants.HUD_HEIGHT_PX));
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        buildStage();
    }

    private void buildStage() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f);
        root.defaults().left();

        root.add(sectionLabel("Scene2D (typography tiers)")).padBottom(10f).row();
        root.add(sceneRow(DawnTypography.TextTier.XS, "XS native 16px: " + SAMPLE)).padBottom(8f).row();
        root.add(sceneRow(DawnTypography.TextTier.S, "S 24px: " + SAMPLE)).padBottom(8f).row();
        root.add(sceneRow(DawnTypography.TextTier.SM, "SM 32px: " + SAMPLE)).padBottom(8f).row();
        root.add(sceneRow(DawnTypography.TextTier.MD, "MD 48px: " + SAMPLE)).padBottom(16f).row();
        root.add(sectionLabel("Press ESC to exit")).row();
        stage.addActor(root);
    }

    private Label sectionLabel(String text) {
        return DawnTypography.label(
                text, fonts, DawnFonts.FontWeight.NORMAL, DawnTypography.TextTier.SM, DawnTypography.TextContext.HUD, DIM);
    }

    private Label sceneRow(DawnTypography.TextTier tier, String text) {
        return DawnTypography.label(
                text, fonts, DawnFonts.FontWeight.NORMAL, tier, DawnTypography.TextContext.HUD, LABEL_COLOR);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        drawBatchSection();
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void drawBatchSection() {
        BitmapFont font = fonts.regular();
        float x = 24f;
        float y = Constants.HUD_HEIGHT_PX - 36f;

        y = drawNativeScale(font, x, y, 1, "Native 1x (16pt): " + ALPHABET);
        y = drawNativeScale(font, x, y, 1, "             " + DIGITS + "  " + LOWER);
        y = drawNativeScale(font, x, y, 2, "Native 2x: " + SAMPLE);
        y = drawNativeScale(font, x, y, 3, "Native 3x: " + SAMPLE);
        y -= 6f;

        drawMetrics(font, x, y - 8f);
    }

    private float drawNativeScale(BitmapFont font, float x, float y, int scale, String text) {
        font.getData().setScale(scale);
        layout.setText(font, text);
        font.setColor(LABEL_COLOR);
        font.draw(batch, layout, x, y);
        font.getData().setScale(1f);
        return y - lineHeightPx * scale - 10f;
    }

    private void drawMetrics(BitmapFont font, float x, float y) {
        String metrics =
                String.format(
                        "FreeType size=%dpt  lineHeight=%.1f  capHeight=%.1f  (m5x7 = 5x7px at 16pt)",
                        DawnFonts.NATIVE_POINT_SIZE,
                        font.getData().lineHeight,
                        font.getData().capHeight);
        font.getData().setScale(1f);
        layout.setText(font, metrics);
        font.setColor(DIM);
        font.draw(batch, layout, x, y);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        fonts.dispose();
    }
}
