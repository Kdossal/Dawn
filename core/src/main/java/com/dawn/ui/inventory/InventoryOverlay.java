package com.dawn.ui.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.DawnTypography;
import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.HudDragSlot;
import com.dawn.ui.HudItemDragSession;
import com.dawn.ui.HudSlotChrome;

public final class InventoryOverlay implements Disposable {
    private final DawnAssets assets;
    private final PlayerInventory inventory;
    private final EquipmentInventory equipment;
    private final HudItemDragSession dragSession;
    private final Stage equipmentStage;

    private final Stage stage;
    private final Table dimLayer;
    private final Group inventoryRoot;
    private final HudDragSlot[] wearSlots = new HudDragSlot[InventoryOverlayDesign.WEAR_SLOT_COUNT];
    private final HudDragSlot[] accessorySlots = new HudDragSlot[InventoryOverlayDesign.ACCESSORY_SLOT_COUNT];
    private final HudDragSlot offhandSlot;
    private final HudDragSlot[] gridSlots = new HudDragSlot[InventoryConstants.SIZE];
    private final Label equipmentLabel;
    private final Label inventoryLabel;

    private boolean open;

    public InventoryOverlay(
            DawnFonts fonts,
            DawnAssets assets,
            PlayerInventory inventory,
            EquipmentInventory equipment,
            HudItemDragSession dragSession,
            Stage equipmentStage) {
        this.assets = assets;
        this.inventory = inventory;
        this.equipment = equipment;
        this.dragSession = dragSession;
        this.equipmentStage = equipmentStage;

        stage = new Stage(new FitViewport(Constants.HUD_WIDTH_PX, Constants.HUD_HEIGHT_PX));
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        dimLayer = new Table();
        dimLayer.setFillParent(true);
        dimLayer.setBackground(dimDrawable(assets));
        stage.addActor(dimLayer);

        inventoryRoot = new Group();
        inventoryRoot.setSize(InventoryOverlayDesign.chromeW(), InventoryOverlayDesign.chromeH());
        inventoryRoot.addActor(new InventoryChrome(assets));

        equipmentLabel = sectionLabel(fonts, "Equipment");
        inventoryLabel = sectionLabel(fonts, "Inventory");
        inventoryRoot.addActor(equipmentLabel);
        inventoryRoot.addActor(inventoryLabel);

        EquipmentSlot[] wearOrder = EquipmentSlot.wearOrder();
        for (int col = 0; col < wearOrder.length; col++) {
            HudDragSlot slot = newInventorySlot(fonts, assets, InventorySlotRef.equipment(wearOrder[col]));
            dragSession.registerEquipSlot(slot);
            wearSlots[col] = slot;
            inventoryRoot.addActor(slot);
        }

        EquipmentSlot[] accessoryOrder = EquipmentSlot.accessoryOrder();
        for (int col = 0; col < accessoryOrder.length; col++) {
            HudDragSlot slot =
                    newInventorySlot(fonts, assets, InventorySlotRef.equipment(accessoryOrder[col]));
            dragSession.registerEquipSlot(slot);
            accessorySlots[col] = slot;
            inventoryRoot.addActor(slot);
        }

        offhandSlot =
                newInventorySlot(fonts, assets, InventorySlotRef.equipment(EquipmentSlot.OFF_HAND));
        dragSession.registerEquipSlot(offhandSlot);
        inventoryRoot.addActor(offhandSlot);

        int idx = 0;
        for (int row = 0; row < InventoryConstants.ROWS; row++) {
            for (int col = 0; col < InventoryConstants.COLS; col++) {
                HudDragSlot slot =
                        newInventorySlot(
                                fonts,
                                assets,
                                InventorySlotRef.grid(InventoryOverlayDesign.gridIndex(col, row)));
                dragSession.registerGridSlot(slot);
                gridSlots[idx++] = slot;
                inventoryRoot.addActor(slot);
            }
        }

        layoutSlots();
        layoutLabels();
        layoutInventoryCenter();
        stage.addActor(inventoryRoot);

        open = false;
        stage.getRoot().setVisible(false);
    }

    public Stage stage() {
        return stage;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean locksHotbarSelection() {
        return dragSession.locksHotbarSelection();
    }

    public int lockedHotbarIndex() {
        return dragSession.lockedHotbarIndex();
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
        dragSession.attachCursorTo(stage);
        dragSession.setInventoryCursorLayout(true);
        dragSession.setActive(true, stage);
        refreshAll();
    }

    public void close() {
        open = false;
        dragSession.close();
        dragSession.setInventoryCursorLayout(false);
        dragSession.attachCursorTo(equipmentStage);
        stage.getRoot().setVisible(false);
    }

    public void act(float delta) {
        if (!open) {
            return;
        }
        if (dragSession.isActive()) {
            dragSession.act(stage);
            dragSession.cursorActor().toFront();
        }
        stage.act(delta);
    }

    public void draw() {
        if (!open) {
            return;
        }
        stage.draw();
    }

    public void refreshAll() {
        for (HudDragSlot slot : wearSlots) {
            slot.refresh(inventory, equipment, assets);
        }
        for (HudDragSlot slot : accessorySlots) {
            slot.refresh(inventory, equipment, assets);
        }
        offhandSlot.refresh(inventory, equipment, assets);
        for (HudDragSlot slot : gridSlots) {
            slot.refresh(inventory, equipment, assets);
        }
        dragSession.refreshCursor();
    }

    public void handleToggleKey() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            toggle();
        }
    }

    public void layoutInventoryCenter() {
        inventoryRoot.setPosition(InventoryOverlayDesign.centerX(), InventoryOverlayDesign.centerY());
    }

    public void onResize(int screenWidth, int screenHeight) {
        stage.getViewport().update(screenWidth, screenHeight, true);
        layoutInventoryCenter();
    }

    private void layoutSlots() {
        Rectangle bounds = new Rectangle();
        float slotPx = InventoryOverlayDesign.slotPx();
        float iconPx = InventoryOverlayDesign.iconPx();

        for (int col = 0; col < wearSlots.length; col++) {
            InventoryOverlayDesign.wearSlotBounds(col, bounds);
            wearSlots[col].setLayoutSize(slotPx, iconPx);
            wearSlots[col].setPosition(bounds.x, bounds.y);
        }

        for (int col = 0; col < accessorySlots.length; col++) {
            InventoryOverlayDesign.accessorySlotBounds(col, bounds);
            accessorySlots[col].setLayoutSize(slotPx, iconPx);
            accessorySlots[col].setPosition(bounds.x, bounds.y);
        }

        InventoryOverlayDesign.offhandBounds(bounds);
        offhandSlot.setLayoutSize(slotPx, iconPx);
        offhandSlot.setPosition(bounds.x, bounds.y);

        int idx = 0;
        for (int row = 0; row < InventoryConstants.ROWS; row++) {
            for (int col = 0; col < InventoryConstants.COLS; col++) {
                InventoryOverlayDesign.gridSlotBounds(col, row, bounds);
                gridSlots[idx].setLayoutSize(slotPx, iconPx);
                gridSlots[idx].setPosition(bounds.x, bounds.y);
                idx++;
            }
        }
    }

    private void layoutLabels() {
        layoutSectionLabel(equipmentLabel, InventoryOverlayDesign.EQUIPMENT_LABEL_ART_Y);
        layoutSectionLabel(inventoryLabel, InventoryOverlayDesign.INVENTORY_LABEL_ART_Y);
    }

    private void layoutSectionLabel(Label label, float artYTop) {
        label.pack();
        label.setPosition(
                InventoryOverlayDesign.sectionLabelX(),
                InventoryOverlayDesign.sectionLabelSceneY(artYTop, label.getHeight()));
    }

    private static Label sectionLabel(DawnFonts fonts, String text) {
        Label label =
                DawnTypography.label(
                        text,
                        fonts,
                        DawnFonts.FontWeight.NORMAL,
                        DawnTypography.INVENTORY_SLOT_COUNT,
                        TextContext.HUD,
                        InventoryUiStyle.LABEL_COLOR);
        label.setAlignment(Align.left);
        label.setTouchable(Touchable.disabled);
        return label;
    }

    private static HudDragSlot newInventorySlot(DawnFonts fonts, DawnAssets assets, InventorySlotRef ref) {
        return new HudDragSlot(
                assets,
                fonts,
                ref,
                HudSlotChrome.DULL,
                DawnTypography.INVENTORY_SLOT_COUNT,
                InventoryOverlayDesign.countPadPx());
    }

    private static Drawable dimDrawable(DawnAssets assets) {
        TextureRegion white = assets.whitePixel;
        return new TextureRegionDrawable(white) {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                Color old = batch.getColor();
                batch.setColor(0f, 0f, 0f, InventoryUiStyle.DIM_ALPHA);
                super.draw(batch, x, y, width, height);
                batch.setColor(old);
            }
        };
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
