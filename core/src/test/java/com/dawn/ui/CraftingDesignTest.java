package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.config.Constants;
import org.junit.jupiter.api.Test;

class CraftingDesignTest {
    @Test
    void gridForCount() {
        assertEquals(3, CraftingDesign.gridForCount(0).cols());
        assertEquals(1, CraftingDesign.gridForCount(0).rows());

        assertEquals(3, CraftingDesign.gridForCount(2).cols());
        assertEquals(1, CraftingDesign.gridForCount(2).rows());

        assertEquals(2, CraftingDesign.gridForCount(4).cols());
        assertEquals(2, CraftingDesign.gridForCount(4).rows());

        assertEquals(3, CraftingDesign.gridForCount(5).cols());
        assertEquals(2, CraftingDesign.gridForCount(5).rows());

        assertEquals(4, CraftingDesign.gridForCount(8).cols());
        assertEquals(2, CraftingDesign.gridForCount(8).rows());
    }

    @Test
    void minRowPanelWidth_threeSlotsAtArtBase() {
        int mult = Constants.HUD_ART_MULT;
        CraftingDesign.Layout two = CraftingDesign.layout(2);
        float cell = CraftingSlotDesign.BASE_CELL_PX * mult;
        float inset = CraftingSlotDesign.BASE_GAP_PX * mult;
        float gap = CraftingSlotDesign.BASE_GAP_PX * mult;
        float expectedW = inset * 2f + 3f * cell + 2f * gap;
        float expectedH = inset * 2f + cell;
        assertEquals(expectedW, two.panelW(), 0.001f);
        assertEquals(expectedH, two.panelH(), 0.001f);
        assertEquals(2, two.slotCount());
    }

    @Test
    void artBaseMinRowIsSeventySixPxWide() {
        float inset = CraftingSlotDesign.BASE_GAP_PX;
        float cell = CraftingSlotDesign.BASE_CELL_PX;
        float gap = CraftingSlotDesign.BASE_GAP_PX;
        float w = inset * 2f + 3f * cell + 2f * gap;
        assertEquals(76f, w, 0.001f);
    }

    @Test
    void emptyLayoutHasNoSlots() {
        CraftingDesign.Layout empty = CraftingDesign.layout(0);
        assertEquals(0, empty.slotCount());
        assertEquals(3, empty.cols());
        assertEquals(1, empty.rows());
    }

    @Test
    void partialGridSlotCount() {
        assertEquals(5, CraftingDesign.layout(5).slotCount());
        assertEquals(6, CraftingDesign.layout(6).slotCount());
    }
}
