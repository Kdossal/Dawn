package com.dawn.ui.inventory.tab;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.dawn.assets.DawnAssets;
import com.dawn.ui.inventory.InventoryDesign;
import com.dawn.ui.inventory.InventoryUiStyle;

/** Equipment / crafting selectors along the top edge of the tab page. */
public final class InventoryTabs extends Group {
    public interface Listener {
        void onTabSelected(InventoryTab tab);
    }

    private final DawnAssets assets;
    private final Image equipmentTab;
    private final Image craftingTab;
    private InventoryTab active = InventoryTab.EQUIPMENT;
    private Listener listener;

    public InventoryTabs(DawnAssets assets) {
        this.assets = assets;
        setSize(
                InventoryDesign.TAB_W * 2 + InventoryDesign.TAB_GAP,
                InventoryDesign.TAB_H);

        equipmentTab = tabButton();
        equipmentTab.setPosition(0f, 0f);
        addActor(equipmentTab);

        craftingTab = tabButton();
        craftingTab.setPosition(InventoryDesign.TAB_W + InventoryDesign.TAB_GAP, 0f);
        addActor(craftingTab);

        wireTab(equipmentTab, InventoryTab.EQUIPMENT);
        wireTab(craftingTab, InventoryTab.CRAFTING);
        setActive(InventoryTab.EQUIPMENT);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public InventoryTab activeTab() {
        return active;
    }

    public void setActive(InventoryTab tab) {
        active = tab;
        refreshTextures();
    }

    private Image tabButton() {
        Image tab = new Image();
        tab.setSize(InventoryDesign.TAB_W, InventoryDesign.TAB_H);
        tab.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        return tab;
    }

    private void wireTab(Image tab, InventoryTab tabKind) {
        tab.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (active == tabKind) {
                            return;
                        }
                        setActive(tabKind);
                        if (listener != null) {
                            listener.onTabSelected(tabKind);
                        }
                    }
                });
    }

    private void refreshTextures() {
        equipmentTab.setDrawable(
                InventoryUiStyle.fixedDrawable(
                        active == InventoryTab.EQUIPMENT
                                ? assets.uiInventory.tabEquipmentActive
                                : assets.uiInventory.tabEquipment));
        craftingTab.setDrawable(
                InventoryUiStyle.fixedDrawable(
                        active == InventoryTab.CRAFTING
                                ? assets.uiInventory.tabCraftingActive
                                : assets.uiInventory.tabCrafting));
    }
}
