package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.dawn.config.Constants;
import com.dawn.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

class EquipmentSidebarDesignTest {
    @Test
    void innerGridAtOneX() {
        assertEquals(42f, EquipmentSidebarDesign.innerGridWidth(), 0.001f);
        assertEquals(86f, EquipmentSidebarDesign.innerGridHeight(), 0.001f);
    }

    @Test
    void panelInsetsAtOneX() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
        int mult = Constants.HUD_ART_MULT;
        assertEquals(EquipmentSidebarDesign.BASE_INSET_LEFT * mult, layout.insetLeft(), 0.001f);
        assertEquals(EquipmentSidebarDesign.BASE_INSET_RIGHT * mult, layout.insetRight(), 0.001f);
        assertEquals(EquipmentSidebarDesign.BASE_INSET_BOTTOM * mult, layout.insetBottom(), 0.001f);
        assertEquals(15f * mult, layout.insetTop(), 0.001f);
    }

    @Test
    void gridAndOffhandShareHorizontalCenter() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
        Rectangle slot = new Rectangle();
        Rectangle offhand = new Rectangle();
        EquipmentSidebarDesign.slotBounds(layout.panelX(), layout, 0, 0, slot);
        EquipmentSidebarDesign.offhandBounds(layout.panelX(), layout, offhand);
        float gridCenterX = slot.x + layout.slotPx() + layout.slotGap() * 0.5f;
        float offhandCenterX = offhand.x + layout.slotPx() * 0.5f;
        assertEquals(gridCenterX, offhandCenterX, 0.001f);
    }

    @Test
    void gridOffsetCentersTwoByFourInContentArea() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
        float contentW = layout.panelW() - layout.insetLeft() - layout.insetRight();
        float gridW = 2f * layout.slotPx() + layout.slotGap();
        assertEquals((contentW - gridW) * 0.5f, EquipmentSidebarDesign.gridOffsetX(layout), 0.001f);
    }

    @Test
    void slotPositionsAtHudScale() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
        assertEquals(Constants.HUD_ART_MULT, layout.multiplier());

        Rectangle slot = new Rectangle();
        EquipmentSidebarDesign.slotBounds(layout.panelX(), layout, 0, 0, slot);
        float expectedX = layout.panelX() + layout.insetLeft() + EquipmentSidebarDesign.gridOffsetX(layout);
        float expectedY = layout.panelY() + layout.panelH() - layout.insetTop() - layout.slotPx();
        assertEquals(expectedX, slot.x, 0.001f);
        assertEquals(expectedY, slot.y, 0.001f);
        assertEquals(layout.slotPx(), slot.width, 0.001f);
        assertEquals(layout.slotPx(), slot.height, 0.001f);

        EquipmentSidebarDesign.slotBounds(layout.panelX(), layout, 1, 3, slot);
        float rightColX =
                layout.panelX()
                        + layout.insetLeft()
                        + EquipmentSidebarDesign.gridOffsetX(layout)
                        + layout.slotPx()
                        + layout.slotGap();
        assertEquals(rightColX, slot.x, 0.001f);
        assertEquals(
                layout.panelY()
                        + layout.panelH()
                        - layout.insetTop()
                        - layout.slotPx()
                        - 3 * (layout.slotPx() + layout.slotGap()),
                slot.y,
                0.001f);
    }

    @Test
    void slotsStayInsidePanel() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
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

    @Test
    void panelFlushWithRightEdge() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
        assertEquals(Constants.HUD_WIDTH_PX - layout.panelW(), layout.panelX(), 0.001f);
        assertEquals(Constants.HUD_WIDTH_PX - layout.tabW(), layout.tabX(), 0.001f);
    }

    @Test
    void tabSlidesWithPanelWhenOpen() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
        float panelX = layout.panelX();
        assertEquals(layout.tabX(), EquipmentSidebarDesign.tabXAtSlide(0f, Constants.HUD_WIDTH_PX, layout), 0.001f);
        assertEquals(
                panelX + EquipmentSidebarDesign.BASE_TAB_ON_PANEL_INSET * layout.multiplier(),
                EquipmentSidebarDesign.tabXAtSlide(1f, panelX, layout),
                0.001f);
    }

    @Test
    void panelSlidesFromOffScreen() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
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
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();
        Rectangle offhand = new Rectangle();
        EquipmentSidebarDesign.offhandBounds(layout.panelX(), layout, offhand);
        float contentW = layout.panelW() - layout.insetLeft() - layout.insetRight();
        float expectedX = layout.panelX() + layout.insetLeft() + (contentW - layout.slotPx()) * 0.5f;
        float expectedY = layout.panelY() + layout.insetBottom();
        assertEquals(expectedX, offhand.x, 0.001f);
        assertEquals(expectedY, offhand.y, 0.001f);
    }
}
