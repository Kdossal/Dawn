package com.dawn.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.dawn.assets.DawnAssets;
import com.dawn.ui.DawnFonts.FontWeight;
import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.DawnTypography.TextTier;

/** Pause menu typography and button chrome. */
public final class PauseUiStyle {
    /** Layout at authored HUD px; typography tiers handle readable text at 3× window. */
    public static final float BUTTON_WIDTH = 320f;
    public static final float BUTTON_HEIGHT = 52f;
    public static final float BUTTON_GAP = 14f;
    public static final float TITLE_BOTTOM_PAD = 48f;

    public static final Color TITLE_COLOR = new Color(0.95f, 0.96f, 0.98f, 1f);
    public static final Color BUTTON_UP = new Color(0.28f, 0.30f, 0.34f, 0.96f);
    public static final Color BUTTON_DOWN = new Color(0.20f, 0.22f, 0.26f, 1f);
    public static final Color BUTTON_DISABLED = new Color(0.22f, 0.23f, 0.26f, 0.75f);
    public static final Color BUTTON_FONT = new Color(0.92f, 0.93f, 0.96f, 1f);
    public static final Color BUTTON_FONT_DISABLED = new Color(0.55f, 0.57f, 0.62f, 1f);

    private PauseUiStyle() {}

    public static Label titleLabel(String text, DawnFonts fonts) {
        return DawnTypography.label(text, fonts, FontWeight.BOLD, TextTier.XL, TextContext.HUD, TITLE_COLOR);
    }

    public static Label sectionLabel(String text, DawnFonts fonts) {
        return DawnTypography.label(text, fonts, FontWeight.NORMAL, TextTier.MD, TextContext.HUD, TITLE_COLOR);
    }

    public static TextButton smallButton(String text, DawnFonts fonts, DawnAssets assets) {
        TextButton button = button(text, fonts, assets);
        DawnTypography.apply(button.getLabel(), TextTier.MD, TextContext.HUD);
        return button;
    }

    public static TextButton button(String text, DawnFonts fonts, DawnAssets assets) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = tintedDrawableInternal(assets.whitePixel, BUTTON_UP);
        style.down = tintedDrawableInternal(assets.whitePixel, BUTTON_DOWN);
        style.disabled = tintedDrawableInternal(assets.whitePixel, BUTTON_DISABLED);
        style.font = fonts.forTier(TextTier.MD);
        style.fontColor = BUTTON_FONT;
        style.downFontColor = BUTTON_FONT;
        style.disabledFontColor = BUTTON_FONT_DISABLED;
        TextButton button = new TextButton(text, style);
        DawnTypography.apply(button.getLabel(), TextTier.MD, TextContext.HUD);
        return button;
    }

    private static Drawable tintedDrawableInternal(TextureRegion region, Color tint) {
        return new TextureRegionDrawable(region) {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                Color old = batch.getColor();
                batch.setColor(tint);
                super.draw(batch, x, y, width, height);
                batch.setColor(old);
            }
        };
    }
}
