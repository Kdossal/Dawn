package com.dawn.ui.inventory;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import com.dawn.ui.HudSlotDesign;
import com.dawn.ui.SlotUi;

/** Art-base layout for the full-screen inventory overlay (scaled by {@link Constants#INVENTORY_ART_MULT}). */
public final class InventoryOverlayDesign {
    public static final float BASE_CHROME_W = 250f;
    public static final float BASE_CHROME_H = 150f;

    public static final float BASE_CELL = HudSlotDesign.BASE_SLOT_PX;
    public static final float BASE_COL_GAP = 4f;
    public static final float BASE_INV_ROW_GAP = 3f;

    public static final float WEAR_ORIGIN_X = 123f;
    public static final float WEAR_ORIGIN_Y = 15f;
    public static final float ACCESSORY_ORIGIN_X = 123f;
    public static final float ACCESSORY_ORIGIN_Y = 40f;
    public static final float OFFHAND_ORIGIN_X = 219f;
    public static final float OFFHAND_ORIGIN_Y = 28f;
    public static final float GRID_ORIGIN_X = 123f;
    public static final float GRID_ORIGIN_Y = 71f;

    /** Left edge of section headers (aligned with slot columns). */
    public static final float SECTION_LABEL_X = WEAR_ORIGIN_X;
    /** Distance from chrome top to label top (1× art). */
    public static final float EQUIPMENT_LABEL_ART_Y = 6f;
    /** Between accessory row bottom (y=60) and inventory grid top (y=71). */
    public static final float INVENTORY_LABEL_ART_Y = 62f;

    public static final int WEAR_SLOT_COUNT = 4;
    public static final int ACCESSORY_SLOT_COUNT = 4;

    private InventoryOverlayDesign() {}

    public static int artMult() {
        return Constants.INVENTORY_ART_MULT;
    }

    public static float chromeW() {
        return BASE_CHROME_W * artMult();
    }

    public static float chromeH() {
        return BASE_CHROME_H * artMult();
    }

    /** Slot frame size on screen (20×1× art @ inventory mult). */
    public static float slotPx() {
        return BASE_CELL * artMult();
    }

    public static float iconPx() {
        return SlotUi.iconPxForSlot(slotPx());
    }

    /** Stack-count inset scaled for inventory overlay slots. */
    public static float countPadPx() {
        return HudSlotDesign.BASE_EDGE_PAD * artMult();
    }

    public static float colStep() {
        return (BASE_CELL + BASE_COL_GAP) * artMult();
    }

    public static float centerX() {
        return (Constants.HUD_WIDTH_PX - chromeW()) / 2f;
    }

    public static float centerY() {
        return (Constants.HUD_HEIGHT_PX - chromeH()) / 2f;
    }

    public static float sectionLabelX() {
        return SECTION_LABEL_X * artMult();
    }

    /** Scene2D y (bottom of label) for a header whose top is {@code artYTop} px below chrome top. */
    public static float sectionLabelSceneY(float artYTop, float labelHeightPx) {
        return chromeH() - artYTop * artMult() - labelHeightPx;
    }

    /** Wear row slot (HEAD → BOOTS left to right). */
    public static void wearSlotBounds(int col, Rectangle out) {
        slotBoundsAt(WEAR_ORIGIN_X + col * (BASE_CELL + BASE_COL_GAP), WEAR_ORIGIN_Y, out);
    }

    /** Accessory row slot (ACCESSORY_1 → 4 left to right). */
    public static void accessorySlotBounds(int col, Rectangle out) {
        slotBoundsAt(ACCESSORY_ORIGIN_X + col * (BASE_CELL + BASE_COL_GAP), ACCESSORY_ORIGIN_Y, out);
    }

    public static void offhandBounds(Rectangle out) {
        slotBoundsAt(OFFHAND_ORIGIN_X, OFFHAND_ORIGIN_Y, out);
    }

    /** Inventory grid slot; row 0 is the top visual row. */
    public static void gridSlotBounds(int col, int row, Rectangle out) {
        float artX = GRID_ORIGIN_X + col * (BASE_CELL + BASE_COL_GAP);
        float artY = GRID_ORIGIN_Y + row * (BASE_CELL + BASE_INV_ROW_GAP);
        slotBoundsAt(artX, artY, out);
    }

    public static int gridIndex(int col, int row) {
        return com.dawn.inventory.PlayerInventory.toIndex(row, col);
    }

    /** Art top-left (x, y down) → Scene2D bounds within chrome group (y-up). */
    private static void slotBoundsAt(float artX, float artY, Rectangle out) {
        float m = artMult();
        float size = slotPx();
        out.set(artX * m, chromeH() - (artY + BASE_CELL) * m, size, size);
    }
}
