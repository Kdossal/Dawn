package com.dawn.ui.inventory;

import com.dawn.config.Constants;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.inventory.InventoryConstants;
import com.dawn.ui.SlotUi;

/**
 * Fixed 1× pixel layout for inventory chrome (scaled by {@link #UI_SCALE} at render).
 *
 * <p>Vertical chrome (148px): grid 60 + tab page 74 + tab selectors 8 + margins = 148.
 *
 * <p>Tab page inner (193−4 × 74−4 = 189×70): armor 16 + char 36 + accessories 16 + stats 112 with 3px gaps.
 */
public final class InventoryDesign {
    public static final float UI_SCALE = 5f;

    public static final int CHROME_W = 200;
    public static final int CHROME_H = 148;

    public static final int SLOT_PX = 16;
    public static final int GAP_PX = 3;
    public static final int GRID_PAD = GAP_PX;

    public static final int SLOT_FIELD_W =
            InventoryConstants.COLS * SLOT_PX + (InventoryConstants.COLS - 1) * GAP_PX;
    public static final int SLOT_FIELD_H =
            InventoryConstants.ROWS * SLOT_PX + (InventoryConstants.ROWS - 1) * GAP_PX;

    public static final int CONTENT_W = GRID_PAD * 2 + SLOT_FIELD_W;
    public static final int GRID_PANEL_H = GRID_PAD * 2 + SLOT_FIELD_H;

    public static final float GRID_X = (CHROME_W - CONTENT_W) / 2f;
    public static final int GRID_BOTTOM_PAD = GRID_PAD;
    public static final int GRID_Y = GRID_BOTTOM_PAD;

    public static final int TAB_PAGE_W = CONTENT_W;
    public static final int TAB_PAGE_H = 74;
    public static final int TAB_W = 12;
    public static final int TAB_H = 8;
    public static final int TAB_GAP = 2;
    /** Page + selectors above (selectors sit on top of the page, not inside it). */
    public static final int TAB_STACK_H = TAB_PAGE_H + TAB_H;

    public static final float TAB_PAGE_X = GRID_X;
    public static final int TAB_PAGE_Y = GRID_Y + GRID_PANEL_H;

    /** Equipment tab content inset (2px so 4×16 + 3×2 wear column = 70px inner height). */
    public static final int PAGE_PAD = 2;
    public static final int PAGE_INNER_W = TAB_PAGE_W - PAGE_PAD * 2;
    public static final int PAGE_INNER_H = TAB_PAGE_H - PAGE_PAD * 2;

    public static final int EQUIP_COL_GAP = 3;
    public static final int EQUIP_SLOT_GAP = 2;
    public static final int WEAR_COLUMN_H = 4 * SLOT_PX + 3 * EQUIP_SLOT_GAP;

    public static final int ARMOR_X = PAGE_PAD;
    public static final int CHAR_X = ARMOR_X + SLOT_PX + EQUIP_COL_GAP;
    public static final int CHAR_W = 36;
    public static final int CHAR_SPRITE_PX = 32;
    public static final int ACCESS_X = CHAR_X + CHAR_W + EQUIP_COL_GAP;
    public static final int STATS_X = ACCESS_X + SLOT_PX + EQUIP_COL_GAP;
    public static final int STATS_W = TAB_PAGE_W - PAGE_PAD - STATS_X;

    public static final int EXP_BAR_W = 28;
    public static final int EXP_BAR_H = 4;
    /** Gap between name baseline and sprite top (larger = sprite lower). */
    public static final int CHAR_NAME_TO_SPRITE_GAP = 5;
    public static final int CHAR_SPRITE_DOWN_OFFSET = 4;
    /** Pixels between sprite bottom and level label top. */
    public static final int CHAR_SPRITE_TO_LEVEL_GAP = 1;
    public static final int CHAR_LEVEL_H = 7;
    public static final int CHAR_LEVEL_TO_EXP_GAP = 1;
    /** Nudge XP bar down without moving level, sprite, or XP text. */
    public static final int CHAR_EXP_BAR_DROP = 4;
    public static final int CHAR_EXP_LABEL_H = 8;
    /** Pixels between XP bar bottom and XP text top. */
    public static final int CHAR_EXP_TO_LABEL_GAP = 2;

    public static final int STAT_COLS = 2;
    public static final int STAT_ROWS = 3;
    public static final int STAT_COL_GAP = 2;
    public static final int STAT_ROW_GAP = 2;
    public static final int STAT_BOX_W = (STATS_W - STAT_COL_GAP) / STAT_COLS;
    public static final int STAT_BOX_H = (PAGE_INNER_H - STAT_ROW_GAP * (STAT_ROWS - 1)) / STAT_ROWS;

    public static final int GRID_ROWS = InventoryConstants.ROWS;
    public static final int GRID_COLS = InventoryConstants.COLS;

    public static float slotIconPx() {
        return SlotUi.iconPxForSlot(SLOT_PX);
    }

    public static float dragIconPx() {
        return slotIconPx() * UI_SCALE;
    }

    public static float scaledChromeWidth() {
        return CHROME_W * UI_SCALE;
    }

    public static float scaledChromeHeight() {
        return CHROME_H * UI_SCALE;
    }

    public static float centerX() {
        return (Constants.HUD_WIDTH_PX - scaledChromeWidth()) / 2f;
    }

    public static float centerY() {
        return (Constants.HUD_HEIGHT_PX - scaledChromeHeight()) / 2f;
    }

    public static float wearSlotY(int indexFromTop) {
        int fromBottom = (EquipmentSlot.wearOrder().length - 1) - indexFromTop;
        return PAGE_PAD + fromBottom * (SLOT_PX + EQUIP_SLOT_GAP);
    }

    public static float accessorySlotY(int indexFromTop) {
        int fromBottom = (EquipmentSlot.accessoryOrder().length - 1) - indexFromTop;
        return PAGE_PAD + fromBottom * (SLOT_PX + EQUIP_SLOT_GAP);
    }

    public static float statBoxX(int col) {
        return STATS_X + col * (STAT_BOX_W + STAT_COL_GAP);
    }

    public static float statBoxY(int rowFromTop) {
        int fromBottom = (STAT_ROWS - 1) - rowFromTop;
        return PAGE_PAD + fromBottom * (STAT_BOX_H + STAT_ROW_GAP);
    }

    private InventoryDesign() {}
}
