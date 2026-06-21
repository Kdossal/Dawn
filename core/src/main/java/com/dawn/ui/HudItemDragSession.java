package com.dawn.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.dawn.assets.DawnAssets;
import com.dawn.entity.Entity;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.ui.inventory.InventoryCursorController;
import com.dawn.ui.inventory.InventorySlotRef;

/** Drag-drop between HUD item bar and equipment sidebar slots. */
public final class HudItemDragSession {
    private final PlayerInventory inventory;
    private final EquipmentInventory equipment;
    private final DawnAssets assets;
    private final InventoryCursorController cursorController;
    private final HudDragCursorActor cursorActor;
    private final HudDragSlot[] hotbarProxies = new HudDragSlot[InventoryConstants.SIZE];
    private final Runnable onChanged;

    private Hotbar hotbar;
    private boolean active;

    public HudItemDragSession(
            PlayerInventory inventory,
            EquipmentInventory equipment,
            DropSystem dropSystem,
            Entity entity,
            DawnAssets assets,
            DawnFonts fonts,
            Runnable onChanged) {
        this.inventory = inventory;
        this.equipment = equipment;
        this.assets = assets;
        this.onChanged = onChanged;
        cursorController =
                new InventoryCursorController(inventory, equipment, dropSystem, entity, this::notifyChanged);
        cursorActor = new HudDragCursorActor(assets, fonts);

        for (int i = 0; i < hotbarProxies.length; i++) {
            HudDragSlot proxy = new HudDragSlot(assets, InventorySlotRef.grid(i), false);
            hotbarProxies[i] = proxy;
            cursorController.registerDragTarget(proxy, proxy.slotRef());
        }
    }

    public void bindHotbar(Hotbar hotbar) {
        this.hotbar = hotbar;
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

    public void setActive(boolean active, Stage stage, EquipmentSidebarDesign.Layout layout) {
        this.active = active;
        if (!active) {
            cursorController.returnCursorToInventory();
            cursorActor.setVisible(false);
            for (HudDragSlot proxy : hotbarProxies) {
                proxy.remove();
            }
            return;
        }
        layoutHotbarProxies(layout);
        for (HudDragSlot proxy : hotbarProxies) {
            if (proxy.getStage() != stage) {
                stage.addActor(proxy);
            }
        }
        refreshCursor();
    }

    public void layoutHotbarProxies(EquipmentSidebarDesign.Layout layout) {
        if (hotbar == null) {
            return;
        }
        Rectangle slot = new Rectangle();
        for (int i = 0; i < hotbarProxies.length; i++) {
            hotbar.slotBounds(i, slot);
            HudDragSlot proxy = hotbarProxies[i];
            proxy.setLayoutSize(slot.width, layout.iconPx());
            proxy.setPosition(slot.x, slot.y);
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
        cursorActor.refresh(cursorController.cursorStack(), assets);
    }

    public void notifyChanged() {
        onChanged.run();
    }

    public void close() {
        if (active) {
            cursorController.returnCursorToInventory();
        }
        active = false;
        cursorActor.setVisible(false);
        for (HudDragSlot proxy : hotbarProxies) {
            proxy.remove();
        }
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
