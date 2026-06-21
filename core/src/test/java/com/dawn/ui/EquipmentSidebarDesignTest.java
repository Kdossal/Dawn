package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.render.GameSettings;
import org.junit.jupiter.api.Test;

class EquipmentSidebarDesignTest {
    @Test
    void innerGridAtOneX() {
        assertEquals(42f, EquipmentSidebarDesign.innerGridWidth(), 0.001f);
        assertEquals(86f, EquipmentSidebarDesign.innerGridHeight(), 0.001f);
    }

    @Test
    void slotPositionsAtMediumUiSize() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout(GameSettings.UiSize.MEDIUM);
        assertEquals(3, layout.multiplier());

        Rectangle slot = new Rectangle();
        EquipmentSidebarDesign.slotBounds(layout.panelX(), layout, 0, 0, slot);
        float expectedX = layout.panelX() + layout.inset();
        float expectedY = layout.panelY() + layout.panelH() - layout.inset() - layout.slotPx();
        assertEquals(expectedX, slot.x, 0.001f);
        assertEquals(expectedY, slot.y, 0.001f);
        assertEquals(layout.slotPx(), slot.width, 0.001f);
        assertEquals(layout.slotPx(), slot.height, 0.001f);

        EquipmentSidebarDesign.slotBounds(layout.panelX(), layout, 1, 3, slot);
        float rightColX = layout.panelX() + layout.inset() + layout.slotPx() + layout.slotGap();
        assertEquals(rightColX, slot.x, 0.001f);
        assertEquals(
                layout.panelY()
                        + layout.panelH()
                        - layout.inset()
                        - layout.slotPx()
                        - 3 * (layout.slotPx() + layout.slotGap()),
                slot.y,
                0.001f);
    }

    @Test
    void slotsStayInsidePanelAtSmallAndMedium() {
        for (GameSettings.UiSize uiSize : new GameSettings.UiSize[] {GameSettings.UiSize.SMALL, GameSettings.UiSize.MEDIUM}) {
            EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout(uiSize);
            Rectangle slot = new Rectangle();
            for (int row = 0; row < EquipmentSidebarDesign.SLOT_ROWS; row++) {
                for (int col = 0; col < EquipmentSidebarDesign.SLOT_COLS; col++) {
                    EquipmentSidebarDesign.slotBounds(layout.panelX(), layout, col, row, slot);
                    assertTrue(slot.x >= layout.panelX());
                    assertTrue(slot.y >= layout.panelY());
                    assertTrue(slot.x + slot.width <= layout.panelX() + layout.panelW());
                    assertTrue(slot.y + slot.height <= layout.panelY() + layout.panelH());
                }
            }
        }
    }

    @Test
    void panelFlushWithRightEdge() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout(GameSettings.UiSize.MEDIUM);
        assertEquals(Constants.HUD_WIDTH_PX - layout.panelW(), layout.panelX(), 0.001f);
        assertEquals(Constants.HUD_WIDTH_PX - layout.tabW(), layout.tabX(), 0.001f);
    }

    @Test
    void tabSlidesWithPanelWhenOpen() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout(GameSettings.UiSize.MEDIUM);
        float panelX = layout.panelX();
        assertEquals(layout.tabX(), EquipmentSidebarDesign.tabXAtSlide(0f, Constants.HUD_WIDTH_PX, layout), 0.001f);
        assertEquals(panelX, EquipmentSidebarDesign.tabXAtSlide(1f, panelX, layout), 0.001f);
    }

    @Test
    void panelSlidesFromOffScreen() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout(GameSettings.UiSize.MEDIUM);
        assertEquals(Constants.HUD_WIDTH_PX, EquipmentSidebarDesign.panelXAtSlide(0f, layout), 0.001f);
        assertEquals(layout.panelX(), EquipmentSidebarDesign.panelXAtSlide(1f, layout), 0.001f);
    }

    @Test
    void slotAtMapsWearAndAccessoryColumns() {
        assertEquals(EquipmentSlot.HEAD, EquipmentSidebarDesign.slotAt(0, 0));
        assertEquals(EquipmentSlot.BOOTS, EquipmentSidebarDesign.slotAt(0, 3));
        assertEquals(EquipmentSlot.ACCESSORY_1, EquipmentSidebarDesign.slotAt(1, 0));
        assertEquals(EquipmentSlot.ACCESSORY_4, EquipmentSidebarDesign.slotAt(1, 3));
    }

    @Test
    void offhandSlotCenteredBelowColumns() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout(GameSettings.UiSize.SMALL);
        Rectangle offhand = new Rectangle();
        EquipmentSidebarDesign.offhandBounds(layout.panelX(), layout, offhand);
        float gridW = EquipmentSidebarDesign.SLOT_COLS * layout.slotPx() + (EquipmentSidebarDesign.SLOT_COLS - 1) * layout.slotGap();
        float expectedX = layout.panelX() + layout.inset() + (gridW - layout.slotPx()) * 0.5f;
        float expectedY = layout.panelY() + layout.inset();
        assertEquals(expectedX, offhand.x, 0.001f);
        assertEquals(expectedY, offhand.y, 0.001f);
    }
}
