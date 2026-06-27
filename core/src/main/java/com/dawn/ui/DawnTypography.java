package com.dawn.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.dawn.ui.DawnFonts.FontWeight;
import com.dawn.ui.inventory.InventoryDesign;

/** Screen-pixel typography tiers for crisp m5x7 rendering. */
public final class DawnTypography {
    /** Matches {@link DawnFonts#NATIVE_POINT_SIZE} atlas line height after load. */
    public static final int BASE_LINE_PX = 16;

    /** Stack count on inventory slots and hotbar (32px screen at 2× reference). */
    public static final TextTier SLOT_COUNT = TextTier.SM;

    public enum TextTier {
        /** Native 1× m5x7 grid (~5×7 px glyphs). */
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
        HUD,
        INVENTORY_DESIGN
    }

    private DawnTypography() {}

    /** HUD batch scale matching inventory slot count labels on screen. */
    public static float slotCountHudScale() {
        return scale(SLOT_COUNT, TextContext.HUD);
    }

    public static float scale(TextTier tier, TextContext context) {
        float base = BASE_LINE_PX;
        if (context == TextContext.INVENTORY_DESIGN) {
            base *= InventoryDesign.UI_SCALE;
        }
        float scale = tier.screenPx / base;
        return scale;
    }

    public static void apply(Label label, TextTier tier, TextContext context) {
        label.setFontScale(scale(tier, context));
    }

    /** Font scale for labels under inventoryRoot (design space; parent applies UI_SCALE once). */
    public static void applyInventory(Label label, TextTier tier) {
        apply(label, tier, TextContext.INVENTORY_DESIGN);
    }

    public static Label label(
            String text,
            DawnFonts fonts,
            FontWeight weight,
            TextTier tier,
            TextContext context,
            Color color) {
        Label label = new Label(text, new Label.LabelStyle(fonts.font(weight), color));
        apply(label, tier, context);
        return label;
    }

    /** Measure text at a tier (updates layout width/height). */
    public static void layout(
            GlyphLayout layout,
            BitmapFont font,
            CharSequence text,
            TextTier tier,
            TextContext context) {
        float s = scale(tier, context);
        font.getData().setScale(s);
        layout.setText(font, text);
        font.getData().setScale(1f);
    }

    /** Draw a pre-measured layout at the same tier scale used for layout. */
    public static void draw(
            BitmapFont font,
            SpriteBatch batch,
            GlyphLayout layout,
            float x,
            float y,
            TextTier tier,
            TextContext context,
            Color color) {
        float s = scale(tier, context);
        font.getData().setScale(s);
        font.setColor(color);
        font.draw(batch, layout, x, y);
        font.getData().setScale(1f);
    }

    /** Layout and draw in one call (scale stays consistent). */
    public static void draw(
            SpriteBatch batch,
            BitmapFont font,
            GlyphLayout layout,
            CharSequence text,
            TextTier tier,
            TextContext context,
            float x,
            float y,
            Color color) {
        float s = scale(tier, context);
        font.getData().setScale(s);
        layout.setText(font, text);
        font.setColor(color);
        font.draw(batch, layout, x, y);
        font.getData().setScale(1f);
    }
}
