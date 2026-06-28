package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;
import com.dawn.entity.Entity;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.render.HudViewport;
import com.dawn.ui.inventory.InventorySlotRef;

/** Right-middle HUD equipment sidebar with tab toggle and drag-drop. */
public final class EquipmentSidebarHud implements Disposable {
    private static final float SLIDE_SPEED = 8f;
    private static final int GRID_SLOT_COUNT =
            EquipmentSidebarDesign.SLOT_ROWS * EquipmentSidebarDesign.SLOT_COLS;

    private final DawnAssets assets;
    private final PlayerInventory inventory;
    private final EquipmentInventory equipment;
    private final HudItemDragSession dragSession;
    private final Hotbar hotbar;

    private final Stage stage;
    private final Group tabButton;
    private final Image tab;
    private final Image panelBg;
    private final HudDragSlot[] equipSlots = new HudDragSlot[GRID_SLOT_COUNT];
    private final HudDragSlot offhandSlot;

    private boolean openTarget;
    private float slideT;
    private boolean inventoryOverlayOpen;
    private boolean dragLayerActive;
    private final Vector2 pointerHud = new Vector2();

    public EquipmentSidebarHud(
            DawnFonts fonts,
            DawnAssets assets,
            PlayerInventory inventory,
            EquipmentInventory equipment,
            DropSystem dropSystem,
            Entity entity,
            Hotbar hotbar,
            HudViewport hudViewport) {
        this.assets = assets;
        this.inventory = inventory;
        this.equipment = equipment;
        this.hotbar = hotbar;

        dragSession =
                new HudItemDragSession(
                        inventory,
                        equipment,
                        dropSystem,
                        entity,
                        assets,
                        fonts,
                        this::refreshAll);

        stage = new Stage(hudViewport.viewport());
        hotbar.attachStage(stage);
        dragSession.bindHotbar(hotbar);

        tabButton = new Group();
        tabButton.setTouchable(Touchable.enabled);
        tabButton.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        toggle();
                    }
                });
        tab = new Image(new TextureRegionDrawable(assets.uiInventory.eqpTab));
        tab.setTouchable(Touchable.disabled);
        tabButton.addActor(tab);
        stage.addActor(tabButton);

        panelBg = new Image(new TextureRegionDrawable(assets.uiInventory.eqpBackground));
        panelBg.setVisible(false);
        panelBg.setTouchable(Touchable.disabled);
        stage.addActor(panelBg);

        int idx = 0;
        for (int row = 0; row < EquipmentSidebarDesign.SLOT_ROWS; row++) {
            for (int col = 0; col < EquipmentSidebarDesign.SLOT_COLS; col++) {
                InventorySlotRef ref =
                        InventorySlotRef.equipment(EquipmentSidebarDesign.slotAt(col, row));
                HudDragSlot slot = new HudDragSlot(assets, fonts, ref, HudSlotChrome.DULL);
                dragSession.registerEquipSlot(slot);
                equipSlots[idx++] = slot;
                slot.setVisible(false);
                stage.addActor(slot);
            }
        }
        offhandSlot = new HudDragSlot(assets, fonts, InventorySlotRef.equipment(com.dawn.inventory.EquipmentSlot.OFF_HAND), HudSlotChrome.DULL);
        dragSession.registerEquipSlot(offhandSlot);
        offhandSlot.setVisible(false);
        stage.addActor(offhandSlot);

        stage.addActor(dragSession.cursorActor());
        syncViewport();
        layout();
        updateDragLayer();
    }

    public void syncViewport() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public Stage stage() {
        return stage;
    }

    public HudItemDragSession dragSession() {
        return dragSession;
    }

    /** User intent: sidebar open or opening. */
    public boolean isOpen() {
        return openTarget;
    }

    public boolean isAnimating() {
        float target = openTarget ? 1f : 0f;
        return Math.abs(slideT - target) > 0.001f;
    }

    public boolean isDragActive() {
        return dragLayerActive;
    }

    public boolean locksHotbarSelection() {
        return dragLayerActive && dragSession.locksHotbarSelection();
    }

    public int lockedHotbarIndex() {
        return dragSession.lockedHotbarIndex();
    }

    public boolean hasHeldCursor() {
        return dragLayerActive && dragSession.cursorController().hasCursor();
    }

    public com.dawn.item.ItemStack cursorStack() {
        return dragSession.cursorController().cursorStack();
    }

    public com.dawn.ui.inventory.InventoryCursorController cursorController() {
        return dragSession.cursorController();
    }

    public com.dawn.item.ItemStack interactionHeld(com.dawn.item.ItemStack hotbarHeld) {
        return hasHeldCursor() ? cursorStack() : hotbarHeld;
    }

    public boolean isPointerOverInteractiveHud() {
        pointerHud.set(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(pointerHud);
        Actor hit = stage.hit(pointerHud.x, pointerHud.y, true);
        return hit != null;
    }

    /** Hotbar hit test in stage/HUD space (use instead of raw screen coords). */
    public boolean isPointerOverHotbar() {
        pointerHud.set(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(pointerHud);
        return hotbar.hitTest(pointerHud.x, pointerHud.y) != null;
    }

    public void dropHeldToWorld() {
        dragSession.cursorController().dropCursorToWorld();
    }

    /** LMB on world while holding a HUD cursor item — drop the stack at the player. */
    public boolean tryWorldDropOnClick() {
        if (!hasHeldCursor() || inventoryOverlayOpen) {
            return false;
        }
        if (!Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            return false;
        }
        if (isPointerOverInteractiveHud()) {
            return false;
        }
        dropHeldToWorld();
        return true;
    }

    public void setInventoryOverlayOpen(boolean inventoryOverlayOpen) {
        this.inventoryOverlayOpen = inventoryOverlayOpen;
        updateDragLayer();
    }

    public void toggle() {
        if (openTarget) {
            close();
        } else {
            open();
        }
    }

    public void open() {
        openTarget = true;
        panelBg.setVisible(true);
        for (HudDragSlot slot : equipSlots) {
            slot.setVisible(true);
        }
        offhandSlot.setVisible(true);
        refreshAll();
        layout();
    }

    public void close() {
        openTarget = false;
        dragSession.close();
        dragLayerActive = false;
        layout();
    }

    public void refreshAll() {
        for (HudDragSlot slot : equipSlots) {
            slot.refresh(inventory, equipment, assets);
        }
        offhandSlot.refresh(inventory, equipment, assets);
        dragSession.refreshCursor();
    }

    public void act(float delta) {
        if (inventoryOverlayOpen) {
            updateDragLayer();
            return;
        }
        syncViewport();

        float target = openTarget ? 1f : 0f;
        if (slideT < target) {
            slideT = Math.min(target, slideT + SLIDE_SPEED * delta);
            layout();
        } else if (slideT > target) {
            slideT = Math.max(target, slideT - SLIDE_SPEED * delta);
            layout();
        }

        if (slideT <= 0f && !openTarget) {
            panelBg.setVisible(false);
            for (HudDragSlot slot : equipSlots) {
                slot.setVisible(false);
            }
            offhandSlot.setVisible(false);
        }

        updateDragLayer();

        if (dragLayerActive) {
            dragSession.act(stage);
            dragSession.cursorActor().toFront();
        }
        stage.act(delta);
    }

    public void draw() {
        if (inventoryOverlayOpen) {
            return;
        }
        syncViewport();
        hotbar.refreshSlots(equipment);
        stage.draw();
    }

    public void onResize(int screenWidth, int screenHeight) {
        stage.getViewport().update(screenWidth, screenHeight, true);
        hotbar.layout();
        layout();
    }

    private void updateDragLayer() {
        if (inventoryOverlayOpen) {
            dragLayerActive = false;
            return;
        }
        if (!dragSession.isActive()) {
            dragSession.setInventoryCursorLayout(false);
            dragSession.attachCursorTo(stage);
            dragSession.setActive(true, stage);
        }
        dragLayerActive = true;
    }

    private void layout() {
        EquipmentSidebarDesign.Layout layout = EquipmentSidebarDesign.layout();

        float panelX = EquipmentSidebarDesign.panelXAtSlide(slideT, layout);
        float tabX = EquipmentSidebarDesign.tabXAtSlide(slideT, panelX, layout);
        boolean tabFlipped = slideT > 0.01f || openTarget;

        tabButton.setSize(layout.tabW(), layout.tabH());
        tabButton.setPosition(tabX, layout.tabY());
        tab.setSize(layout.tabW(), layout.tabH());
        tab.setOrigin(0f, 0f);
        tab.setPosition(0f, 0f);
        tab.setScaleX(1f);
        if (tabFlipped) {
            tabButton.setOrigin(0f, 0f);
            tabButton.setScaleX(-1f);
        } else {
            tabButton.setOrigin(0f, 0f);
            tabButton.setScaleX(1f);
        }

        panelBg.setSize(layout.panelW(), layout.panelH());
        panelBg.setPosition(panelX, layout.panelY());

        Rectangle slotBounds = new Rectangle();
        int idx = 0;
        for (int row = 0; row < EquipmentSidebarDesign.SLOT_ROWS; row++) {
            for (int col = 0; col < EquipmentSidebarDesign.SLOT_COLS; col++) {
                EquipmentSidebarDesign.slotBounds(panelX, layout, col, row, slotBounds);
                HudDragSlot slot = equipSlots[idx++];
                slot.setLayoutSize(layout.slotPx(), layout.iconPx());
                slot.setPosition(slotBounds.x, slotBounds.y);
            }
        }
        EquipmentSidebarDesign.offhandBounds(panelX, layout, slotBounds);
        offhandSlot.setLayoutSize(layout.slotPx(), layout.iconPx());
        offhandSlot.setPosition(slotBounds.x, slotBounds.y);

        tabButton.toFront();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
