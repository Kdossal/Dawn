package com.dawn.ui.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.inventory.PlayerInventory;
import com.dawn.inventory.PlayerProfile;
import com.dawn.entity.Entity;
import com.dawn.item.ItemStack;
import com.dawn.ui.DawnFonts;

public final class InventoryOverlay implements Disposable {
    private final Stage stage;
    private final Table dimLayer;
    private final Group inventoryRoot;
    private final InventoryChrome chrome;
    private final InventoryCursorController cursorController;
    private final InventoryCursorActor cursorActor;
    private final ItemTooltip tooltip;
    private final PlayerInventory inventory;
    private final EquipmentInventory equipment;
    private final PlayerProfile profile;
    private final DawnAssets assets;
    private boolean open;

    public InventoryOverlay(
            DawnFonts fonts,
            DawnAssets assets,
            PlayerInventory inventory,
            EquipmentInventory equipment,
            PlayerProfile profile,
            DropSystem dropSystem,
            Entity entity) {
        this.assets = assets;
        this.inventory = inventory;
        this.equipment = equipment;
        this.profile = profile;

        stage = new Stage(new FitViewport(Constants.HUD_WIDTH_PX, Constants.HUD_HEIGHT_PX));
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        dimLayer = new Table();
        dimLayer.setFillParent(true);
        dimLayer.setBackground(dimDrawable(assets));
        stage.addActor(dimLayer);

        chrome =
                new InventoryChrome(assets, fonts, inventory, equipment, profile);
        inventoryRoot = new Group();
        inventoryRoot.setSize(InventoryDesign.CHROME_W, InventoryDesign.CHROME_H);
        inventoryRoot.setTransform(true);
        inventoryRoot.setScale(InventoryDesign.UI_SCALE);
        inventoryRoot.addActor(chrome);
        layoutInventoryCenter();
        stage.addActor(inventoryRoot);

        cursorController =
                new InventoryCursorController(
                        inventory, equipment, dropSystem, entity, this::refreshAll);
        chrome.gridPanel().registerCursorController(cursorController);
        chrome.tabStack().equipmentPanel().registerCursorController(cursorController);
        cursorController.registerWorldDropTarget(dimLayer);

        cursorActor = new InventoryCursorActor(assets, fonts);
        stage.addActor(cursorActor);

        tooltip = new ItemTooltip(assets, fonts);
        tooltip.attach(stage);
        bindTooltips();

        close();
    }

    public Stage stage() {
        return stage;
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        if (open) {
            close();
        } else {
            open();
        }
    }

    public void open() {
        open = true;
        stage.getRoot().setVisible(true);
        refreshAll();
    }

    public void close() {
        cursorController.returnCursorToInventory();
        open = false;
        stage.getRoot().setVisible(false);
        cursorActor.refresh(ItemStack.empty(), assets);
    }

    public void act(float delta) {
        if (!open) {
            return;
        }
        stage.act(delta);
        cursorActor.followMouse(stage);
    }

    public void draw() {
        if (!open) {
            return;
        }
        stage.draw();
    }

    public void refreshAll() {
        if (!open) {
            return;
        }
        chrome.refresh(inventory, equipment, profile, assets);
        cursorActor.refresh(cursorController.cursorStack(), assets);
    }

    public void handleToggleKey() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            toggle();
        }
    }

    public void layoutInventoryCenter() {
        inventoryRoot.setPosition(InventoryDesign.centerX(), InventoryDesign.centerY());
    }

    public void onResize(int screenWidth, int screenHeight) {
        stage.getViewport().update(screenWidth, screenHeight, true);
        layoutInventoryCenter();
    }

    private void bindTooltips() {
        for (int r = 0; r < com.dawn.inventory.InventoryConstants.ROWS; r++) {
            for (int c = 0; c < com.dawn.inventory.InventoryConstants.COLS; c++) {
                int index = PlayerInventory.toIndex(r, c);
                ItemSlotWidget slot = chrome.gridPanel().slotAt(r, c);
                tooltip.bind(slot, () -> inventory.getSlotAtIndex(index));
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemSlotWidget widget = chrome.tabStack().equipmentPanel().equipmentSlotWidget(slot);
            if (widget != null) {
                tooltip.bind(widget, () -> equipment.get(slot));
            }
        }
    }

    private static Drawable dimDrawable(DawnAssets assets) {
        TextureRegion white = assets.whitePixel;
        TextureRegionDrawable drawable = new TextureRegionDrawable(white) {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                Color old = batch.getColor();
                batch.setColor(0f, 0f, 0f, InventoryUiStyle.DIM_ALPHA);
                super.draw(batch, x, y, width, height);
                batch.setColor(old);
            }
        };
        return drawable;
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
