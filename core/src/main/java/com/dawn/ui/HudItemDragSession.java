package com.dawn.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.dawn.assets.DawnAssets;
import com.dawn.entity.Entity;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.ui.inventory.InventoryCursorController;
import com.dawn.ui.inventory.InventorySlotRef;
import com.dawn.world.storage.CrateStorage;

/** Drag-drop between HUD item bar and equipment sidebar slots. */
public final class HudItemDragSession {
    private final InventoryCursorController cursorController;
    private final HudDragCursorActor cursorActor;
    private final Runnable onChanged;

    private boolean active;
    private Runnable extraOnChanged;

    public HudItemDragSession(
            PlayerInventory inventory,
            EquipmentInventory equipment,
            DropSystem dropSystem,
            Entity entity,
            DawnAssets assets,
            DawnFonts fonts,
            Runnable onChanged) {
        this.onChanged = onChanged;
        cursorController =
                new InventoryCursorController(inventory, equipment, dropSystem, entity, this::notifyChanged);
        cursorActor = new HudDragCursorActor(assets, fonts);
    }

    public void bindHotbar(Hotbar hotbar) {
        for (int i = 0; i < HudSlotDesign.SLOT_COUNT; i++) {
            HudDragSlot slot = hotbar.slotAt(i);
            cursorController.registerDragTarget(slot, slot.slotRef());
        }
    }

    public HudDragCursorActor cursorActor() {
        return cursorActor;
    }

    public InventoryCursorController cursorController() {
        return cursorController;
    }

    public void registerEquipSlot(HudDragSlot slot) {
        cursorController.registerDragTarget(slot, slot.slotRef());
    }

    public void registerContainerSlot(HudDragSlot slot) {
        cursorController.registerDragTarget(slot, slot.slotRef());
    }

    public void setContainer(CrateStorage container) {
        cursorController.setContainer(container);
    }

    public void clearContainer() {
        cursorController.setContainer(null);
    }

    public CrateStorage container() {
        return cursorController.container();
    }

    public void setExtraOnChanged(Runnable extraOnChanged) {
        this.extraOnChanged = extraOnChanged;
    }

    public void setActive(boolean active, Stage stage) {
        this.active = active;
        if (!active) {
            cursorController.returnCursorToInventory();
            cursorActor.setVisible(false);
        } else {
            refreshCursor();
        }
    }

    public void act(Stage stage) {
        if (!active) {
            return;
        }
        refreshCursor();
        cursorActor.followMouse(stage);
    }

    public void refreshCursor() {
        cursorActor.refresh(cursorController.cursorStack());
    }

    public void notifyChanged() {
        onChanged.run();
        if (extraOnChanged != null) {
            extraOnChanged.run();
        }
    }

    public void close() {
        if (active) {
            cursorController.returnCursorToInventory();
        }
        active = false;
        cursorActor.setVisible(false);
    }

    public boolean isActive() {
        return active;
    }

    public boolean locksHotbarSelection() {
        if (!active || !cursorController.hasCursor()) {
            return false;
        }
        InventorySlotRef origin = cursorController.cursorOrigin();
        return origin != null && origin.kind == InventorySlotRef.Kind.GRID;
    }

    public int lockedHotbarIndex() {
        InventorySlotRef origin = cursorController.cursorOrigin();
        if (origin == null || origin.kind != InventorySlotRef.Kind.GRID) {
            return -1;
        }
        return origin.gridIndex;
    }
}
