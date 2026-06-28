package com.dawn.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.dawn.ui.DawnFonts.FontWeight;

/** Screen-pixel typography tiers — each tier uses a native m5x7 atlas at its target size. */
public final class DawnTypography {
    /** Stack count on inventory slots and hotbar ({@link TextTier#SM}). */
    public static final TextTier SLOT_COUNT = TextTier.SM;
    /** Stack count on full-screen inventory overlay ({@link TextTier#MD}). */
    public static final TextTier INVENTORY_SLOT_COUNT = TextTier.MD;

    public enum TextTier {
        /** Native ~16px line (m5x7 grid). */
        XS(16),
        SM(32),
        MD(48),
        LG(64),
        XL(96);

        private final int screenPx;

        TextTier(int screenPx) {
            this.screenPx = screenPx;
        }

        public int screenPx() {
            return screenPx;
        }
    }

    public enum TextContext {
        HUD
    }

    private DawnTypography() {}

    /** HUD batch scale matching inventory slot count labels on screen. */
    public static float slotCountHudScale() {
        return scale(SLOT_COUNT, TextContext.HUD);
    }

    public static float scale(TextTier tier, TextContext context) {
        return 1f;
    }

    public static void apply(Label label, TextTier tier, TextContext context) {
        label.setFontScale(scale(tier, context));
    }

    public static Label label(
            String text,
            DawnFonts fonts,
            FontWeight weight,
            TextTier tier,
            TextContext context,
            Color color) {
        Label label = new Label(text, new Label.LabelStyle(fonts.forTier(tier), color));
        apply(label, tier, context);
        return label;
    }

    /** Measure text at a tier (updates layout width/height). */
    public static void layout(
            GlyphLayout layout, DawnFonts fonts, CharSequence text, TextTier tier, TextContext context) {
        BitmapFont font = fonts.forTier(tier);
        float s = scale(tier, context);
        font.getData().setScale(s);
        layout.setText(font, text);
        font.getData().setScale(1f);
    }

    /** Layout and draw in one call (scale stays consistent). */
    public static void draw(
            SpriteBatch batch,
            DawnFonts fonts,
            GlyphLayout layout,
            CharSequence text,
            TextTier tier,
            TextContext context,
            float x,
            float y,
            Color color) {
        BitmapFont font = fonts.forTier(tier);
        float s = scale(tier, context);
        font.getData().setScale(s);
        layout.setText(font, text);
        font.setColor(color);
        font.draw(batch, layout, x, y);
        font.getData().setScale(1f);
    }
}
