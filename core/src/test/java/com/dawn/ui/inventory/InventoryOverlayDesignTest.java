package com.dawn.ui.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import com.dawn.ui.HudSlotDesign;
import org.junit.jupiter.api.Test;

class InventoryOverlayDesignTest {
    @Test
    void artBaseChromeSize() {
        assertEquals(250f, InventoryOverlayDesign.BASE_CHROME_W, 0.001f);
        assertEquals(150f, InventoryOverlayDesign.BASE_CHROME_H, 0.001f);
    }

    @Test
    void scaledChromeSizeAtInventoryArtMult() {
        int mult = Constants.INVENTORY_ART_MULT;
        assertEquals(250f * mult, InventoryOverlayDesign.chromeW(), 0.001f);
        assertEquals(150f * mult, InventoryOverlayDesign.chromeH(), 0.001f);
        assertEquals(1250f, InventoryOverlayDesign.chromeW(), 0.001f);
        assertEquals(750f, InventoryOverlayDesign.chromeH(), 0.001f);
    }

    @Test
    void inventorySlotsLargerThanHudHotbar() {
        assertEquals(100f, InventoryOverlayDesign.slotPx(), 0.001f);
        assertEquals(HudSlotDesign.slotPx(), 60f, 0.001f);
    }

    @Test
    void centersOnHud() {
        float cx = InventoryOverlayDesign.centerX();
        float cy = InventoryOverlayDesign.centerY();
        assertEquals(Constants.HUD_WIDTH_PX, cx + InventoryOverlayDesign.chromeW() + cx, 0.001f);
        assertEquals(Constants.HUD_HEIGHT_PX, cy + InventoryOverlayDesign.chromeH() + cy, 0.001f);
    }

    @Test
    void wearAndGridAnchorsAtMult5() {
        int mult = Constants.INVENTORY_ART_MULT;
        Rectangle bounds = new Rectangle();

        InventoryOverlayDesign.wearSlotBounds(0, bounds);
        assertEquals(InventoryOverlayDesign.WEAR_ORIGIN_X * mult, bounds.x, 0.001f);
        assertEquals(
                InventoryOverlayDesign.chromeH()
                        - (InventoryOverlayDesign.WEAR_ORIGIN_Y + InventoryOverlayDesign.BASE_CELL) * mult,
                bounds.y,
                0.001f);
        assertEquals(100f, bounds.width, 0.001f);

        InventoryOverlayDesign.gridSlotBounds(0, 0, bounds);
        assertEquals(InventoryOverlayDesign.GRID_ORIGIN_X * mult, bounds.x, 0.001f);
        assertEquals(
                InventoryOverlayDesign.chromeH()
                        - (InventoryOverlayDesign.GRID_ORIGIN_Y + InventoryOverlayDesign.BASE_CELL) * mult,
                bounds.y,
                0.001f);

        InventoryOverlayDesign.gridSlotBounds(0, 1, bounds);
        float row1ArtY =
                InventoryOverlayDesign.GRID_ORIGIN_Y
                        + InventoryOverlayDesign.BASE_CELL
                        + InventoryOverlayDesign.BASE_INV_ROW_GAP;
        assertEquals(
                InventoryOverlayDesign.chromeH() - (row1ArtY + InventoryOverlayDesign.BASE_CELL) * mult,
                bounds.y,
                0.001f);

        InventoryOverlayDesign.offhandBounds(bounds);
        assertEquals(InventoryOverlayDesign.OFFHAND_ORIGIN_X * mult, bounds.x, 0.001f);
        assertEquals(
                InventoryOverlayDesign.chromeH()
                        - (InventoryOverlayDesign.OFFHAND_ORIGIN_Y + InventoryOverlayDesign.BASE_CELL) * mult,
                bounds.y,
                0.001f);
    }
}
