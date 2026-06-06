package com.dawn.ui.inventory.tab;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.inventory.InventoryDesign;
import com.dawn.ui.inventory.InventoryUiStyle;

/** Placeholder crafting tab until recipes are implemented. */
public final class CraftingTabPanel extends Group {
    public CraftingTabPanel(DawnFonts fonts) {
        setSize(InventoryDesign.TAB_PAGE_W, InventoryDesign.TAB_PAGE_H);
        Label soon = InventoryUiStyle.label("Crafting — soon", fonts);
        soon.setAlignment(Align.center);
        soon.setSize(InventoryDesign.TAB_PAGE_W, InventoryDesign.TAB_PAGE_H);
        addActor(soon);
    }
}
