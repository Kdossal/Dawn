package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.dawn.ui.DawnTypography.TextTier;

/**
 * Loads m5x7 from TTF via FreeType — one crisp atlas per {@link TextTier} screen size (no upscale
 * from a tiny base glyph).
 *
 * <p>m5x7 glyphs are ~5×7 px when rasterized at 16pt; {@link TextTier#XS} uses that native size.
 */
public final class DawnFonts implements Disposable {
    public enum FontWeight {
        NORMAL,
        BOLD,
        ITALIC
    }

    private static final String TTF_PATH = "fonts/m5x7.ttf";
    /** Smallest tier raster size (m5x7 native grid). */
    public static final int NATIVE_POINT_SIZE = TextTier.XS.screenPx();

    private static final Color SHADOW_COLOR = new Color(0f, 0f, 0f, 0.72f);

    private final FreeTypeFontGenerator generator;
    private final ObjectMap<TextTier, BitmapFont> tierFonts = new ObjectMap<>();

    public DawnFonts() {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(TTF_PATH));
        for (TextTier tier : TextTier.values()) {
            tierFonts.put(tier, generate(tier.screenPx()));
        }
    }

    /** Atlas rasterized at the tier's target screen line height. */
    public BitmapFont forTier(TextTier tier) {
        return tierFonts.get(tier);
    }

    public int lineHeightPx(TextTier tier) {
        return Math.round(forTier(tier).getData().lineHeight);
    }

    public BitmapFont small() {
        return forTier(TextTier.XS);
    }

    /** Default body font ({@link TextTier#SM}). */
    public BitmapFont regular() {
        return forTier(TextTier.SM);
    }

    public BitmapFont bold() {
        return regular();
    }

    public BitmapFont italic() {
        return regular();
    }

    public BitmapFont font(FontWeight weight) {
        return regular();
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
        int shadowPx = Math.max(1, pointSize / 16);
        param.shadowOffsetX = 0;
        param.shadowOffsetY = shadowPx;
        param.shadowColor = SHADOW_COLOR;
        param.padTop = 0;
        param.padBottom = 0 * 2;
        param.padLeft = 0;
        param.padRight = 0 * 2;
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS;

        BitmapFont font = generator.generateFont(param);
        font.getData().setScale(1f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return font;
    }

    @Override
    public void dispose() {
        for (ObjectMap.Entry<TextTier, BitmapFont> entry : tierFonts) {
            entry.value.dispose();
        }
        tierFonts.clear();
        generator.dispose();
    }
}
