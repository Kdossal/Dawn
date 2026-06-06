package com.dawn.ui.inventory.tab;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.dawn.assets.DawnAssets;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerProfile;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.equipment.EquipmentTabPanel;
import com.dawn.ui.inventory.InventoryDesign;
import com.dawn.ui.inventory.InventoryUiStyle;

/**
 * Tab page with selectors on top (attached above the page). Equipment / crafting content swaps inside
 * the page.
 */
public final class InventoryTabStack extends Group {
    private final Image tabPage;
    private final EquipmentTabPanel equipmentPanel;
    private final CraftingTabPanel craftingPanel;
    private final InventoryTabs tabs;

    public InventoryTabStack(
            DawnAssets assets,
            DawnFonts fonts,
            EquipmentInventory equipment,
            PlayerProfile profile) {
        setSize(InventoryDesign.TAB_PAGE_W, InventoryDesign.TAB_STACK_H);

        tabPage = new Image(InventoryUiStyle.fixedDrawable(assets.uiInventory.tabPage));
        tabPage.setSize(InventoryDesign.TAB_PAGE_W, InventoryDesign.TAB_PAGE_H);
        tabPage.setPosition(0f, 0f);
        addActor(tabPage);

        equipmentPanel = new EquipmentTabPanel(assets, fonts);
        equipmentPanel.setPosition(0f, 0f);
        addActor(equipmentPanel);

        craftingPanel = new CraftingTabPanel(fonts);
        craftingPanel.setPosition(0f, 0f);
        craftingPanel.setVisible(false);
        addActor(craftingPanel);

        tabs = new InventoryTabs(assets);
        tabs.setPosition(0f, InventoryDesign.TAB_PAGE_H);
        addActor(tabs);

        tabs.setListener(this::showTab);
        equipmentPanel.refresh(profile, equipment, assets);
        showTab(tabs.activeTab());
    }

    public InventoryTabs tabs() {
        return tabs;
    }

    public EquipmentTabPanel equipmentPanel() {
        return equipmentPanel;
    }

    public InventoryTab activeTab() {
        return tabs.activeTab();
    }

    public void refresh(PlayerProfile profile, EquipmentInventory equipment, DawnAssets assets) {
        equipmentPanel.refresh(profile, equipment, assets);
    }

    private void showTab(InventoryTab tab) {
        boolean equipment = tab == InventoryTab.EQUIPMENT;
        equipmentPanel.setVisible(equipment);
        craftingPanel.setVisible(!equipment);
    }
}
