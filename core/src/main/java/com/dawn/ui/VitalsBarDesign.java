package com.dawn.ui;

import com.dawn.config.Constants;

/** Art-base layout and value mapping for top-right HP/EP HUD bars. */
public final class VitalsBarDesign {
    public static final int BASE_EMPTY_W = 102;
    public static final int BASE_EMPTY_H = 8;
    public static final int BASE_LEFT_CAP_W = 2;
    public static final int BASE_FILL_W = 100;
    public static final int BASE_FILL_H = 4;
    public static final int BASE_FILL_INSET_X = 2;
    public static final int BASE_FILL_INSET_Y = 2;
    public static final int BASE_ICON_H = 13;
    public static final int BASE_HP_ICON_W = 13;
    public static final int BASE_EP_ICON_W = 13;
    public static final int BASE_ICON_OVERLAP_W = 4;
    public static final int BASE_BAR_Y_FROM_ICON_BOTTOM = 3;

    public static final int BASE_SMALL_EMPTY_W = 52;
    public static final int BASE_SMALL_EMPTY_H = 6;
    public static final int BASE_SMALL_LEFT_CAP_W = 2;
    public static final int BASE_SMALL_FILL_W = 50;
    public static final int BASE_SMALL_FILL_H = 2;
    public static final int BASE_SMALL_FILL_INSET_X = 2;
    public static final int BASE_SMALL_FILL_INSET_Y = 2; // center 2px strip in 6px frame
    public static final int BASE_SMALL_ICON_W = 11;
    public static final int BASE_SMALL_ICON_H = 11;
    public static final int BASE_SMALL_ICON_OVERLAP_W = 4;
    public static final int BASE_SMALL_BAR_Y_FROM_ICON_BOTTOM = 2;

    public static final int BASE_MARGIN_TOP = 8;
    public static final int BASE_MARGIN_RIGHT = 8;
    public static final int BASE_ROW_GAP = 2;

    private VitalsBarDesign() {}

    public enum Row {
        HP(BASE_HP_ICON_W),
        EP(BASE_EP_ICON_W);

        private final int iconW;

        Row(int iconW) {
            this.iconW = iconW;
        }

        public int iconW() {
            return iconW;
        }
    }

    public record Layout(
            int multiplier,
            int marginTop,
            int marginRight,
            int rowGap,
            int emptyH,
            int leftCapW,
            int fillH,
            int fillInsetX,
            int fillInsetY,
            int iconH,
            int iconOverlapW,
            int barYFromIconBottom,
            int hpIconW,
            int epIconW,
            int smallEmptyH,
            int smallLeftCapW,
            int smallFillH,
            int smallFillInsetX,
            int smallFillInsetY,
            int smallIconW,
            int smallIconH,
            int smallIconOverlapW,
            int smallBarYFromIconBottom) {}

    public record RowLayout(int iconX, int iconY, int barX, int barY) {}

    public static Layout layout() {
        int m = Constants.VITALS_ART_MULT;
        return new Layout(
                m,
                BASE_MARGIN_TOP * m,
                BASE_MARGIN_RIGHT * m,
                BASE_ROW_GAP * m,
                BASE_EMPTY_H * m,
                BASE_LEFT_CAP_W * m,
                BASE_FILL_H * m,
                BASE_FILL_INSET_X * m,
                BASE_FILL_INSET_Y * m,
                BASE_ICON_H * m,
                BASE_ICON_OVERLAP_W * m,
                BASE_BAR_Y_FROM_ICON_BOTTOM * m,
                BASE_HP_ICON_W * m,
                BASE_EP_ICON_W * m,
                BASE_SMALL_EMPTY_H * m,
                BASE_SMALL_LEFT_CAP_W * m,
                BASE_SMALL_FILL_H * m,
                BASE_SMALL_FILL_INSET_X * m,
                BASE_SMALL_FILL_INSET_Y * m,
                BASE_SMALL_ICON_W * m,
                BASE_SMALL_ICON_H * m,
                BASE_SMALL_ICON_OVERLAP_W * m,
                BASE_SMALL_BAR_Y_FROM_ICON_BOTTOM * m);
    }

    /** 1 stat point = 2px at 1x, clamped to authored 100px bar body. */
    public static int bodyPxAtOneX(float maxValue) {
        int px = Math.round(maxValue * 2f);
        return clamp(px, 1, BASE_FILL_W);
    }

    /** 1 stat point = 2px at 1x, clamped to [0, bodyPxAtOneX(maxValue)]. */
    public static int fillPxAtOneX(float currentValue, float maxValue) {
        int body = bodyPxAtOneX(maxValue);
        int fill = Math.round(currentValue * 2f);
        return clamp(fill, 0, body);
    }

    public static RowLayout rowLayout(Layout layout, Row row, int rowIndex, int bodyPxAtOneX) {
        int iconW = row == Row.HP ? layout.hpIconW() : layout.epIconW();
        int iconRight = Constants.HUD_WIDTH_PX - layout.marginRight();
        int iconX = iconRight - iconW;
        int rowTop = Constants.HUD_HEIGHT_PX - layout.marginTop() - rowIndex * (layout.iconH() + layout.rowGap());
        int iconY = rowTop - layout.iconH();

        int barW = scaledBarWidth(layout, bodyPxAtOneX);
        int barX = iconX - (barW - layout.iconOverlapW());
        int barY = iconY + layout.barYFromIconBottom();
        return new RowLayout(iconX, iconY, barX, barY);
    }

    public static int scaledBarWidth(Layout layout, int bodyPxAtOneX) {
        return (BASE_LEFT_CAP_W + clamp(bodyPxAtOneX, 1, BASE_FILL_W)) * layout.multiplier();
    }

    public static int scaledFillWidth(Layout layout, int fillPxAtOneX) {
        return clamp(fillPxAtOneX, 0, BASE_FILL_W) * layout.multiplier();
    }

    /** 0..100 continuous value mapped to 0..50 fill pixels (1x) for small bars. */
    public static int smallFillPxAtOneX(float valuePercent) {
        int px = Math.round(clamp01(valuePercent / 100f) * BASE_SMALL_FILL_W);
        return clamp(px, 0, BASE_SMALL_FILL_W);
    }

    /** True when current value is full or overcapped. */
    public static boolean isFull(float currentValue, float maxValue) {
        return maxValue > 0f && currentValue >= maxValue;
    }

    public static RowLayout smallRowLayout(Layout layout, int rowIndex) {
        int iconW = layout.smallIconW();
        int iconRight = Constants.HUD_WIDTH_PX - layout.marginRight();
        int iconX = iconRight - iconW;
        int rowTop = Constants.HUD_HEIGHT_PX - layout.marginTop() - rowOffset(layout, rowIndex);
        int iconY = rowTop - layout.smallIconH();

        int barW = scaledSmallBarWidth(layout);
        int barX = iconX - (barW - layout.smallIconOverlapW());
        int barY = iconY + layout.smallBarYFromIconBottom();
        return new RowLayout(iconX, iconY, barX, barY);
    }

    public static int scaledSmallBarWidth(Layout layout) {
        return BASE_SMALL_EMPTY_W * layout.multiplier();
    }

    public static int scaledSmallFillWidth(Layout layout, int fillPxAtOneX) {
        return clamp(fillPxAtOneX, 0, BASE_SMALL_FILL_W) * layout.multiplier();
    }

    private static int rowOffset(Layout layout, int rowIndex) {
        return switch (rowIndex) {
            case 0 -> 0;
            case 1 -> layout.iconH() + layout.rowGap();
            case 2 -> (layout.iconH() + layout.rowGap()) * 2;
            case 3 -> (layout.iconH() + layout.rowGap()) * 2 + layout.smallIconH() + layout.rowGap();
            default -> 0;
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
