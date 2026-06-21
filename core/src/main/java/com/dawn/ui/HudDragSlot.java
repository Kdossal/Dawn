package com.dawn.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.dawn.assets.DawnAssets;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.ui.inventory.InventorySlotRef;

/** HUD-sized slot hit target with optional visible chrome for drag-drop. */
public final class HudDragSlot extends Group {
    private final InventorySlotRef slotRef;
    private final Image bg;
    private final Image icon;

    public HudDragSlot(DawnAssets assets, InventorySlotRef slotRef, boolean visible) {
        this.slotRef = slotRef;
        setTouchable(Touchable.enabled);

        if (visible) {
            bg = new Image(new TextureRegionDrawable(assets.uiCommon.slotEquip));
            icon = new Image();
            icon.setScaling(Scaling.fit);
            icon.setAlign(Align.center);
            addActor(bg);
            addActor(icon);
        } else {
            bg = null;
            icon = null;
        }
    }

    public InventorySlotRef slotRef() {
        return slotRef;
    }

    public void setLayoutSize(float slotPx, float iconPx) {
        setSize(slotPx, slotPx);
        if (bg != null) {
            bg.setSize(slotPx, slotPx);
            bg.setPosition(0f, 0f);
        }
        if (icon != null) {
            float ix = (slotPx - iconPx) / 2f;
            float iy = (slotPx - iconPx) / 2f;
            icon.setSize(iconPx, iconPx);
            icon.setPosition(ix, iy);
        }
    }

    public void refresh(PlayerInventory inventory, EquipmentInventory equipment, DawnAssets assets) {
        if (icon == null) {
            return;
        }
        ItemStack stack = stackAt(inventory, equipment);
        if (stack.isEmpty()) {
            TextureRegion guide = guideIcon(assets);
            icon.setDrawable(guide == null ? null : new TextureRegionDrawable(guide));
            icon.setVisible(guide != null);
            return;
        }
        icon.setVisible(true);
        ItemDef def = ItemRegistry.get(stack);
        if (def != null && assets.item(def.iconId()) != null) {
            icon.setDrawable(new TextureRegionDrawable(assets.item(def.iconId())));
        } else {
            icon.setDrawable(null);
        }
    }

    private ItemStack stackAt(PlayerInventory inventory, EquipmentInventory equipment) {
        return switch (slotRef.kind) {
            case GRID -> inventory.getSlotAtIndex(slotRef.gridIndex);
            case EQUIPMENT -> equipment.get(slotRef.equipmentSlot);
        };
    }

    private TextureRegion guideIcon(DawnAssets assets) {
        if (slotRef.kind != InventorySlotRef.Kind.EQUIPMENT) {
            return null;
        }
        EquipmentSlot slot = slotRef.equipmentSlot;
        return switch (slot) {
            case HEAD -> assets.uiCommon.headIcon;
            case CHEST -> assets.uiCommon.bodyIcon;
            case PANTS -> assets.uiCommon.legIcon;
            case BOOTS -> assets.uiCommon.feetIcon;
            case ACCESSORY_1, ACCESSORY_2, ACCESSORY_3, ACCESSORY_4 -> assets.uiCommon.accessoryIcon;
            case OFF_HAND -> assets.uiCommon.handIcon;
        };
    }
}
