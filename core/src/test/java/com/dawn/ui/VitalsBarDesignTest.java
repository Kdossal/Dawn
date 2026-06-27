package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.config.Constants;
import org.junit.jupiter.api.Test;

class VitalsBarDesignTest {
    @Test
    void bodyAndFillMappingClampToAuthoredRange() {
        assertEquals(1, VitalsBarDesign.bodyPxAtOneX(0f));
        assertEquals(50, VitalsBarDesign.bodyPxAtOneX(25f));
        assertEquals(100, VitalsBarDesign.bodyPxAtOneX(500f));

        assertEquals(0, VitalsBarDesign.fillPxAtOneX(-1f, 25f));
        assertEquals(16, VitalsBarDesign.fillPxAtOneX(8f, 25f));
        assertEquals(50, VitalsBarDesign.fillPxAtOneX(40f, 25f));
    }

    @Test
    void scaledDimensionsUseVitalsArtMult() {
        VitalsBarDesign.Layout layout = VitalsBarDesign.layout();

        assertEquals(Constants.VITALS_ART_MULT, layout.multiplier());
        assertEquals(VitalsBarDesign.BASE_EMPTY_H * Constants.VITALS_ART_MULT, layout.emptyH());
        assertEquals(VitalsBarDesign.BASE_FILL_H * Constants.VITALS_ART_MULT, layout.fillH());
        assertEquals(52, layout.hpIconW());
        assertEquals(52, layout.epIconW());
        assertEquals(44, layout.smallIconW());
        assertEquals(VitalsBarDesign.BASE_SMALL_FILL_H * Constants.VITALS_ART_MULT, layout.smallFillH());
    }

    @Test
    void topRightAnchoringAndOverlapMatchSpec() {
        VitalsBarDesign.Layout layout = VitalsBarDesign.layout();
        int body = VitalsBarDesign.bodyPxAtOneX(25f);
        VitalsBarDesign.RowLayout hp = VitalsBarDesign.rowLayout(layout, VitalsBarDesign.Row.HP, 0, body);

        int iconRight = hp.iconX() + layout.hpIconW();
        assertEquals(Constants.HUD_WIDTH_PX - layout.marginRight(), iconRight);

        int barRight = hp.barX() + VitalsBarDesign.scaledBarWidth(layout, body);
        assertEquals(hp.iconX() + layout.iconOverlapW(), barRight);
        assertEquals(hp.iconY() + layout.barYFromIconBottom(), hp.barY());
    }

    @Test
    void secondRowStacksBelowFirstByBarHeightAndGap() {
        VitalsBarDesign.Layout layout = VitalsBarDesign.layout();
        int body = VitalsBarDesign.bodyPxAtOneX(25f);
        VitalsBarDesign.RowLayout hp = VitalsBarDesign.rowLayout(layout, VitalsBarDesign.Row.HP, 0, body);
        VitalsBarDesign.RowLayout ep = VitalsBarDesign.rowLayout(layout, VitalsBarDesign.Row.EP, 1, body);

        assertEquals(hp.iconY() - (layout.iconH() + layout.rowGap()), ep.iconY());
    }

    @Test
    void smallBarFillMaps0to100Into50px() {
        assertEquals(0, VitalsBarDesign.smallFillPxAtOneX(-1f));
        assertEquals(4, VitalsBarDesign.smallFillPxAtOneX(8f));
        assertEquals(25, VitalsBarDesign.smallFillPxAtOneX(50f));
        assertEquals(50, VitalsBarDesign.smallFillPxAtOneX(100f));
        assertEquals(50, VitalsBarDesign.smallFillPxAtOneX(120f));
    }

    @Test
    void fullDetectionTreatsOvercapAsFull() {
        assertEquals(false, VitalsBarDesign.isFull(0f, 0f));
        assertEquals(false, VitalsBarDesign.isFull(24f, 25f));
        assertEquals(true, VitalsBarDesign.isFull(25f, 25f));
        assertEquals(true, VitalsBarDesign.isFull(30f, 25f));
    }

    @Test
    void smallRowsRightAlignAndStackBelowEnergy() {
        VitalsBarDesign.Layout layout = VitalsBarDesign.layout();
        VitalsBarDesign.RowLayout hunger = VitalsBarDesign.smallRowLayout(layout, 2);
        VitalsBarDesign.RowLayout thirst = VitalsBarDesign.smallRowLayout(layout, 3);
        assertEquals(Constants.HUD_WIDTH_PX - layout.marginRight(), hunger.iconX() + layout.smallIconW());
        assertEquals(hunger.iconY() - (layout.smallIconH() + layout.rowGap()), thirst.iconY());
    }
}
