package com.dawn.ui.inventory;

import com.dawn.ui.DawnFonts;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.dawn.assets.DawnAssets;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.ui.inventory.tab.InventoryTabStack;
import com.dawn.inventory.PlayerProfile;

/**
 * Fixed 200×148 chrome: tab page + selectors above, inventory grid anchored to the bottom.
 */
public final class InventoryChrome extends Group {
    private final InventoryTabStack tabStack;
    private final InventoryGridPanel gridPanel;

    public InventoryChrome(
            DawnAssets assets,
            DawnFonts fonts,
            PlayerInventory inventory,
            EquipmentInventory equipment,
            PlayerProfile profile) {
        setSize(InventoryDesign.CHROME_W, InventoryDesign.CHROME_H);

        Image chromeBg = new Image(InventoryUiStyle.fixedDrawable(assets.uiInventory.chromeBg));
        chromeBg.setSize(InventoryDesign.CHROME_W, InventoryDesign.CHROME_H);
        chromeBg.setPosition(0f, 0f);
        addActor(chromeBg);

        tabStack = new InventoryTabStack(assets, fonts, equipment, profile);
        tabStack.setPosition(InventoryDesign.TAB_PAGE_X, InventoryDesign.TAB_PAGE_Y);
        addActor(tabStack);

        gridPanel = new InventoryGridPanel(assets, fonts, inventory);
        gridPanel.setPosition(InventoryDesign.GRID_X, InventoryDesign.GRID_Y);
        addActor(gridPanel);
    }

    public InventoryTabStack tabStack() {
        return tabStack;
    }

    public InventoryGridPanel gridPanel() {
        return gridPanel;
    }

    public void refresh(
            PlayerInventory inventory,
            EquipmentInventory equipment,
            PlayerProfile profile,
            DawnAssets assets) {
        gridPanel.refresh(inventory, assets);
        tabStack.refresh(profile, equipment, assets);
    }
}
