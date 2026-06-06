package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

/**
 * Loads m5x7 from TTF via FreeType.
 *
 * <p>m5x7 glyphs are 5×7 pixels when rasterized at {@link #NATIVE_POINT_SIZE} — not 8. At smaller
 * point sizes FreeType produces sub-pixel, chunky shapes (~4px cap height).
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

    private final FreeTypeFontGenerator generator;
    private final BitmapFont regular;
    private final int lineHeightPx;

    public DawnFonts() {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(TTF_PATH));
        regular = generate();
        lineHeightPx = Math.round(regular.getData().lineHeight);
    }

    public BitmapFont regular() {
        return regular;
    }

    public BitmapFont bold() {
        return regular;
    }

    public BitmapFont italic() {
        return regular;
    }

    public BitmapFont font(FontWeight weight) {
        return regular;
    }

    /** Atlas line height in pixels (use for typography tier math). */
    public int lineHeightPx() {
        return lineHeightPx;
    }

    private BitmapFont generate() {
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = NATIVE_POINT_SIZE;
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
        regular.dispose();
        generator.dispose();
    }
}
