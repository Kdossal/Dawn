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

/** Smoke-test m5x7 tier-native atlases (one raster size per typography tier, baked shadow). */
public final class FontPreviewScreen implements Screen, Disposable {
    private static final Color BG = new Color(0.08f, 0.09f, 0.12f, 1f);
    private static final Color LABEL_COLOR = new Color(0.92f, 0.94f, 0.97f, 1f);
    private static final Color DIM = new Color(0.55f, 0.58f, 0.65f, 1f);
    private static final String SAMPLE = "DAWN — hotbar / inventory / pause";

    private final DawnFonts fonts;
    private final SpriteBatch batch;
    private final GlyphLayout layout;
    private final Stage stage;

    public FontPreviewScreen() {
        fonts = new DawnFonts();
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

        root.add(sectionLabel("Scene2D (tier-native atlases @1×)")).padBottom(10f).row();
        for (DawnTypography.TextTier tier : DawnTypography.TextTier.values()) {
            root.add(sceneRow(tier, tier.name() + " " + tier.screenPx() + "px: " + SAMPLE))
                    .padBottom(8f)
                    .row();
        }
        root.add(sectionLabel("Press ESC to exit")).padTop(8f).row();
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
        float x = 24f;
        float y = Constants.HUD_HEIGHT_PX - 36f;
        for (DawnTypography.TextTier tier : DawnTypography.TextTier.values()) {
            y = drawTierNative(tier, x, y, tier.name() + " @1×: " + SAMPLE);
        }
        drawMetrics(fonts.forTier(DawnTypography.TextTier.XS), x, y - 12f);
    }

    private float drawTierNative(DawnTypography.TextTier tier, float x, float y, String text) {
        BitmapFont font = fonts.forTier(tier);
        layout.setText(font, text);
        font.setColor(LABEL_COLOR);
        font.draw(batch, layout, x, y);
        return y - fonts.lineHeightPx(tier) - 10f;
    }

    private void drawMetrics(BitmapFont font, float x, float y) {
        String metrics =
                String.format(
                        "XS raster=%dpt  lineHeight=%.1f  capHeight=%.1f  (shadow baked per tier)",
                        DawnFonts.NATIVE_POINT_SIZE,
                        font.getData().lineHeight,
                        font.getData().capHeight);
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
