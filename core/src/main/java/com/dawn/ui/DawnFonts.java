package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.render.GameSettings;

/**
 * Loads m5x7 from TTF via FreeType.
 *
 * <p>m5x7 glyphs are 5×7 pixels when rasterized at {@link #NATIVE_POINT_SIZE} — not 8. At smaller
 * point sizes FreeType produces sub-pixel, chunky shapes (~4px cap height).
 *
 * <p>Middle UI size uses a separate {@link #MEDIUM_POINT_SIZE} bake; large UI draws {@link #small}
 * at 2× scale.
 */
public final class DawnFonts implements Disposable {
    public enum FontWeight {
        NORMAL,
        BOLD,
        ITALIC
    }

    private static final String TTF_PATH = "fonts/m5x7.ttf";
    /** m5x7 TTF em square maps 320×448 font units → 5×7 px at this size. */
    public static final int NATIVE_POINT_SIZE = 16;
    /** Crisp middle tier for {@link GameSettings.UiSize#MEDIUM} (~1.5× small ink, integer bake). */
    public static final int MEDIUM_POINT_SIZE = 24;

    private final FreeTypeFontGenerator generator;
    private final BitmapFont small;
    private final BitmapFont medium;
    private final int lineHeightPx;

    public DawnFonts() {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(TTF_PATH));
        small = generate(NATIVE_POINT_SIZE);
        medium = generate(MEDIUM_POINT_SIZE);
        lineHeightPx = Math.round(small.getData().lineHeight);
    }

    /** Small / large UI tier atlas (16pt). */
    public BitmapFont small() {
        return small;
    }

    /** Medium UI tier atlas (24pt). */
    public BitmapFont medium() {
        return medium;
    }

    public BitmapFont regular() {
        return small;
    }

    public BitmapFont bold() {
        return small;
    }

    public BitmapFont italic() {
        return small;
    }

    public BitmapFont font(FontWeight weight) {
        return small;
    }

    public BitmapFont forUiSize(GameSettings.UiSize size) {
        return size == GameSettings.UiSize.MEDIUM ? medium : small;
    }

    /** Draw scale for {@link #forUiSize}; large doubles the small atlas. */
    public static float drawScaleForUiSize(GameSettings.UiSize size) {
        return size == GameSettings.UiSize.LARGE ? 2f : 1f;
    }

    /** Atlas line height in pixels (use for typography tier math). */
    public int lineHeightPx() {
        return lineHeightPx;
    }

    private BitmapFont generate(int pointSize) {
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = pointSize;
        param.minFilter = Texture.TextureFilter.Nearest;
        param.magFilter = Texture.TextureFilter.Nearest;
        param.genMipMaps = false;
        param.kerning = false;
        param.hinting = FreeTypeFontGenerator.Hinting.None;
        param.mono = false;
        param.renderCount = 1;
        param.gamma = 1.8f;
        param.borderWidth = 0f;
        param.shadowOffsetX = 0;
        param.shadowOffsetY = 0;
        param.padTop = 0;
        param.padBottom = 0;
        param.padLeft = 0;
        param.padRight = 0;
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS;

        BitmapFont font = generator.generateFont(param);
        font.getData().setScale(1f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return font;
    }

    @Override
    public void dispose() {
        small.dispose();
        medium.dispose();
        generator.dispose();
    }
}
