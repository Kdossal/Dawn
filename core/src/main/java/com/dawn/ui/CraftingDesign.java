package com.dawn.ui;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;

/** Art-base layout for the in-world crafting popup (dynamic grid, scaled by {@link Constants#HUD_ART_MULT}). */
public final class CraftingDesign {
    public static final int MAX_RECIPES = 8;
    public static final int MIN_GRID_COLS = 3;
    public static final int MIN_GRID_ROWS = 1;

    /** Panel padding between frame and slot grid (at 1× art). */
    public static final float BASE_INSET = HudPanelDesign.BASE_INSET;

    private CraftingDesign() {}

    public record Grid(int cols, int rows) {}

    public record Layout(
            float panelW,
            float panelH,
            float cellPx,
            float iconPx,
            float inset,
            float slotGap,
            int cols,
            int rows,
            int slotCount) {}

    /** Grid dimensions for a recipe count (0 = empty panel with min 1×3 footprint). */
    public static Grid gridForCount(int recipeCount) {
        int n = Math.max(0, Math.min(recipeCount, MAX_RECIPES));
        if (n == 0) {
            return new Grid(MIN_GRID_COLS, MIN_GRID_ROWS);
        }
        if (n <= 3) {
            return new Grid(3, 1);
        }
        if (n == 4) {
            return new Grid(2, 2);
        }
        if (n <= 6) {
            return new Grid(3, 2);
        }
        return new Grid(4, 2);
    }

    public static Layout layout(int recipeCount) {
        Grid grid = gridForCount(recipeCount);
        int mult = Constants.HUD_ART_MULT;
        float cellPx = CraftingSlotDesign.cellPx();
        float iconPx = SlotUi.iconPxForSlot(cellPx);
        float inset = BASE_INSET * mult;
        float slotGap = CraftingSlotDesign.gapPx();
        float panelW = inset * 2f + grid.cols * cellPx + (grid.cols - 1) * slotGap;
        float panelH = inset * 2f + grid.rows * cellPx + (grid.rows - 1) * slotGap;
        int slots = recipeCount <= 0 ? 0 : Math.min(recipeCount, grid.cols * grid.rows);
        return new Layout(panelW, panelH, cellPx, iconPx, inset, slotGap, grid.cols, grid.rows, slots);
    }

    public static void slotBounds(float panelX, float panelY, Layout layout, int col, int row, Rectangle out) {
        float x = panelX + layout.inset() + col * (layout.cellPx() + layout.slotGap());
        float y =
                panelY
                        + layout.panelH()
                        - layout.inset()
                        - layout.cellPx()
                        - row * (layout.cellPx() + layout.slotGap());
        out.set(x, y, layout.cellPx(), layout.cellPx());
    }
}
